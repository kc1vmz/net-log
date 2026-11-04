(function () {
    "use strict";

    const list = document.getElementById("archived-event-list");
    const details = document.getElementById("archived-event-details");
    if (!list || !details) {
        return;
    }

    const rows = Array.from(list.querySelectorAll("tr[data-archived-event-id]"));
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
            const link = row.querySelector(".archived-event-selection");
            if (selected) {
                link.setAttribute("aria-current", "true");
            } else {
                link.removeAttribute("aria-current");
            }
        });
    }

    async function selectArchivedEvent(row, updateHistory) {
        const version = ++requestVersion;
        if (pendingRequest) {
            pendingRequest.abort();
        }
        markSelection(row);
        details.setAttribute("aria-busy", "false");

        if (!row) {
            showMessage("Select an archived event to view details.");
            return;
        }

        const id = row.dataset.archivedEventId;
        if (updateHistory) {
            const url = new URL(window.location.href);
            if (url.searchParams.get("selected") !== id) {
                url.searchParams.set("selected", id);
                window.history.pushState(null, "", url);
            }
        }

        pendingRequest = new AbortController();
        details.setAttribute("aria-busy", "true");
        showMessage("Loading archived event details…");

        try {
            const response = await fetch("/archivedEvent/" + encodeURIComponent(id) + "/details-pane", {
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
        } catch (error) {
            if (error.name === "AbortError" || version !== requestVersion) {
                return;
            }
            showMessage(error.message === "missing"
                ? "This archived event is no longer available. Select another event."
                : "Unable to load archived event details. Select the event again to retry.");
        } finally {
            if (version === requestVersion) {
                details.setAttribute("aria-busy", "false");
            }
        }
    }

    list.addEventListener("click", function (event) {
        const row = event.target.closest("tr[data-archived-event-id]");
        if (!row || event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) {
            return;
        }
        event.preventDefault();
        selectArchivedEvent(row, true);
    });

    window.addEventListener("popstate", function () {
        const id = new URL(window.location.href).searchParams.get("selected");
        const row = rows.find(function (candidate) {
            return candidate.dataset.archivedEventId === id;
        });
        selectArchivedEvent(row || null, false);
    });
}());
