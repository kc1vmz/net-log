(function () {
    "use strict";

    const dialog = document.getElementById("action-dialog");
    if (!dialog || typeof dialog.showModal !== "function") {
        return;
    }

    const title = document.getElementById("action-dialog-title");
    const body = document.getElementById("action-dialog-body");
    let pendingRequest = null;
    let parentDialog = null;

    function restoreParentDialog() {
        const parent = parentDialog;
        parentDialog = null;
        if (pendingRequest) {
            pendingRequest.abort();
            pendingRequest = null;
        }
        title.textContent = parent.title;
        dialog.className = parent.className;
        body.replaceChildren(parent.form);
        body.setAttribute("aria-busy", "false");
        const fields = parent.form.querySelector(".action-dialog-fields");
        if (fields) {
            fields.scrollTop = parent.scrollTop;
        }
        parent.trigger.focus();
    }

    function cancelButton(label) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "block action-dialog-cancel";
        button.textContent = label;
        button.addEventListener("click", function () {
            if (parentDialog) {
                restoreParentDialog();
            } else {
                dialog.close();
            }
        });
        return button;
    }

    async function openDialog(trigger, url = trigger.href, requestOptions = {}) {
        const mode = trigger.dataset.actionDialog;
        const dismissLabel = mode === "report" ? "Close" : "Cancel";
        const subject = trigger.dataset.dialogSubject;
        title.textContent = trigger.dataset.dialogTitle + (subject ? " — " + subject : "");
        dialog.classList.toggle("action-dialog-delete", mode === "delete");
        dialog.classList.toggle("action-dialog-edit", mode === "edit" || mode === "participants");
        dialog.classList.toggle("action-dialog-participants", mode === "participants");
        body.setAttribute("aria-busy", "true");

        const request = new AbortController();
        pendingRequest = request;
        const loading = document.createElement("p");
        loading.textContent = "Loading…";
        body.replaceChildren(loading, cancelButton(dismissLabel));
        if (!dialog.open) {
            dialog.showModal();
        }

        try {
            // Keep the original server-rendered fields, values, validation,
            // POST action, and download/redirect behavior.
            const response = await fetch(url, {
                ...requestOptions,
                signal: request.signal,
                headers: { "Accept": "text/html" }
            });
            if (!response.ok) {
                throw new Error("Unable to load form");
            }
            const formDocument = new DOMParser().parseFromString(await response.text(), "text/html");
            if (request.signal.aborted || !dialog.open) {
                return;
            }
            const form = formDocument.querySelector("form");
            const submit = form && form.querySelector('input[type="submit"]');
            const cancel = form && Array.from(form.querySelectorAll("a")).find(function (link) {
                return link.textContent.trim() === "Cancel" || link.textContent.trim() === "Close";
            });
            if (!form || !submit || !cancel) {
                throw new Error("Form is unavailable");
            }

            submit.value = trigger.dataset.dialogSubmit || submit.value;
            submit.classList.add("action-dialog-primary");
            submit.parentElement.classList.add("action-dialog-actions");
            cancel.replaceWith(cancelButton(dismissLabel));

            if (mode === "steps" && !form.querySelector('select[name="locationId"]')) {
                // Let the existing POST handler choose the next step and follow
                // its redirect inside the dialog. The final Update submits normally.
                form.addEventListener("submit", function (event) {
                    event.preventDefault();
                    openDialog(trigger, form.action, {
                        method: "POST",
                        body: new URLSearchParams(new FormData(form))
                    });
                });
            }

            if (mode === "edit" || mode === "participants") {
                const actions = submit.parentElement;
                const fields = document.createElement("div");
                fields.className = "action-dialog-fields";
                Array.from(form.childNodes).forEach(function (node) {
                    if (node !== actions) {
                        fields.appendChild(node);
                    }
                });
                form.prepend(fields);
                const defaults = fields.querySelector("[data-participation-defaults]");
                if (defaults) {
                    form.insertBefore(defaults, actions);
                }
            }

            if (trigger.dataset.dialogWarning) {
                const warning = document.createElement("p");
                warning.className = "action-dialog-warning";
                warning.textContent = trigger.dataset.dialogWarning;
                form.querySelector("div").appendChild(warning);
            }

            body.replaceChildren(form);
            if (window.NetlogParticipantSelection) {
                window.NetlogParticipantSelection.initialize(form);
            }
            const firstField = form.querySelector('input:not([type="submit"]):not([type="hidden"]), select, textarea');
            if (firstField && mode !== "delete") {
                firstField.focus();
            } else {
                form.querySelector("button").focus();
            }
        } catch (error) {
            if (error.name !== "AbortError" && dialog.open && pendingRequest === request) {
                const message = document.createElement("p");
                message.setAttribute("role", "alert");
                message.textContent = "Unable to load this form. Cancel and try again.";
                body.replaceChildren(message, cancelButton(dismissLabel));
            }
        } finally {
            if (pendingRequest === request) {
                body.setAttribute("aria-busy", "false");
            }
        }
    }

    document.addEventListener("click", function (event) {
        const trigger = event.target.closest("a[data-action-dialog]");
        if (!trigger || event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) {
            return;
        }
        event.preventDefault();
        const parentForm = trigger.closest("#action-dialog form");
        parentDialog = parentForm ? {
            form: parentForm,
            title: title.textContent,
            className: dialog.className,
            scrollTop: parentForm.querySelector(".action-dialog-fields")?.scrollTop || 0,
            trigger: trigger
        } : null;
        openDialog(trigger);
    });

    dialog.addEventListener("cancel", function (event) {
        if (parentDialog) {
            event.preventDefault();
            restoreParentDialog();
        }
    });

    dialog.addEventListener("close", function () {
        if (pendingRequest) {
            pendingRequest.abort();
            pendingRequest = null;
        }
        body.replaceChildren();
        parentDialog = null;
        body.setAttribute("aria-busy", "false");
    });
}());
