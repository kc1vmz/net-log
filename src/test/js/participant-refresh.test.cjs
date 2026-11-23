const assert = require("node:assert/strict");
const { readFileSync } = require("node:fs");
const path = require("node:path");
const { test } = require("node:test");
const vm = require("node:vm");

const script = readFileSync(path.join(__dirname,
    "../../main/resources/static/js/participant-selection.js"), "utf8");

function row(id, value = "2026-11-23T19:30") {
    const input = { value };
    return { id, querySelector: () => input };
}

function setup(response) {
    const listeners = {};
    const button = {
        dataset: { refreshUrl: "/refresh" },
        addEventListener: (name, callback) => { listeners[name] = callback; }
    };
    const status = { textContent: "" };
    const body = {
        rows: [row("existing", "2026-11-23T20:15")],
        replaceChildren(...rows) { this.rows = rows; }
    };
    const excluded = [row("selected"), row("previous")];
    const form = {
        dataset: { participantSelection: "checkin" },
        matches: () => true,
        querySelector(selector) {
            return {
                "[data-refresh-participants]": button,
                "[data-refresh-status]": status,
                "#operator-list tbody": body
            }[selector] || {};
        },
        querySelectorAll: () => excluded,
        addEventListener() {}
    };
    const freshRows = [row("existing"), row("new"), row("selected"), row("previous")];
    vm.runInNewContext(script, {
        window: {},
        document: {
            querySelectorAll: () => [form],
            createElement: () => ({ querySelector: () => ({ rows: freshRows }) })
        },
        fetch: response
    });
    return { refresh: listeners.click, button, status, body, excluded };
}

test("refresh excludes both other lists and preserves edited times", async () => {
    const state = setup(async () => ({ ok: true, text: async () => "rows" }));
    await state.refresh();
    assert.deepEqual(Array.from(state.body.rows, item => item.id), ["existing", "new"]);
    assert.equal(state.body.rows[0].querySelector().value, "2026-11-23T20:15");
    assert.equal(state.button.disabled, false);
    assert.match(state.status.textContent, /refreshed \(2\)/);
});

test("selections made while refreshing are excluded when the response arrives", async () => {
    let finish;
    const state = setup(() => new Promise(resolve => { finish = resolve; }));
    const pending = state.refresh();
    assert.equal(state.button.disabled, true);
    state.excluded.push(row("new"));
    finish({ ok: true, text: async () => "rows" });
    await pending;
    assert.deepEqual(Array.from(state.body.rows, item => item.id), ["existing"]);
});

test("a failed refresh leaves the existing list intact and allows retry", async () => {
    const state = setup(async () => ({ ok: false }));
    const original = state.body.rows;
    await state.refresh();
    assert.equal(state.body.rows, original);
    assert.equal(state.button.disabled, false);
    assert.match(state.status.textContent, /Unable to refresh/);
});
