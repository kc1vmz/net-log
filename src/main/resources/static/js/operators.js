(function () {
    "use strict";

    const list = document.getElementById("operator-list");
    const details = document.getElementById("operator-details");
    if (!list || !details) {
        return;
    }

    const rows = Array.from(list.querySelectorAll("tr[data-operator-id]"));
    let pendingRequest = null;
    let requestVersion = 0;

    function showMessage(message) {
        const paragraph = document.createElement("p");
        paragraph.textContent = message;
        details.replaceChildren(paragraph);
    }

    function markSelection(selectedRow) {
        rows.forEach(function (row) {
            const selected = row === selectedRow;
            row.classList.toggle("is-selected", selected);
            const link = row.querySelector(".operator-selection");
            if (selected) {
                link.setAttribute("aria-current", "true");
            } else {
                link.removeAttribute("aria-current");
            }
        });
    }

    async function selectOperator(row, updateHistory) {
        const version = ++requestVersion;
        if (pendingRequest) {
            pendingRequest.abort();
        }
        markSelection(row);
        details.setAttribute("aria-busy", "false");

        if (!row) {
            showMessage("Select an operator to view details.");
            return;
        }

        const id = row.dataset.operatorId;
        if (updateHistory) {
            const url = new URL(window.location.href);
            if (url.searchParams.get("selected") !== id) {
                url.searchParams.set("selected", id);
                window.history.pushState(null, "", url);
            }
        }

        pendingRequest = new AbortController();
        details.setAttribute("aria-busy", "true");
        showMessage("Loading operator details…");

        try {
            const response = await fetch("/operator/" + encodeURIComponent(id) + "/details-pane", {
                signal: pendingRequest.signal,
                headers: { "Accept": "text/html" }
            });
            if (!response.ok) {
                throw new Error(response.status === 404 ? "missing" : "load");
            }
            const html = await response.text();
            if (version !== requestVersion) {
                return;
            }

            const template = document.createElement("template");
            template.innerHTML = html;
            details.replaceChildren(template.content.cloneNode(true));
            details.scrollTop = 0;
            if (window.matchMedia("(max-width: 760px)").matches) {
                details.scrollIntoView({ block: "start" });
            }
        } catch (error) {
            if (error.name === "AbortError" || version !== requestVersion) {
                return;
            }
            showMessage(error.message === "missing"
                ? "This operator is no longer available. Select another operator."
                : "Unable to load operator details. Select the operator again to retry.");
        } finally {
            if (version === requestVersion) {
                details.setAttribute("aria-busy", "false");
            }
        }
    }

    list.addEventListener("click", function (event) {
        const row = event.target.closest("tr[data-operator-id]");
        if (!row || event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) {
            return;
        }
        event.preventDefault();
        selectOperator(row, true);
    });

    window.addEventListener("popstate", function () {
        const id = new URL(window.location.href).searchParams.get("selected");
        const row = rows.find(function (candidate) {
            return candidate.dataset.operatorId === id;
        });
        selectOperator(row || null, false);
    });
}());
