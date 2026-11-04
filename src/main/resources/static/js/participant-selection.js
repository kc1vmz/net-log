(function () {
    "use strict";

    function initialize(form) {
        if (!form.matches("form[data-participant-selection]") || form.dataset.selectionInitialized) {
            return;
        }
        form.dataset.selectionInitialized = "true";
        const selected = new Map();
        const ids = form.querySelector('[name="participantIds"]');
        const checkIn = form.dataset.participantSelection === "checkin";
        const availableLabel = checkIn ? "Check-in" : "Check-out";

        function updatePayload() {
            if (!checkIn) {
                ids.value = Array.from(selected, function (entry) {
                    return entry[0] + "," + entry[1] + ",";
                }).join("");
                return;
            }
            const payload = form.querySelector("[data-checkin-payload]");
            payload.replaceChildren();
            Array.from(selected.values()).forEach(function (participant, index) {
                Object.entries(participant).forEach(function (entry) {
                    const input = document.createElement("input");
                    input.type = "hidden";
                    input.name = "participants[" + index + "]." + entry[0];
                    input.value = entry[1];
                    payload.appendChild(input);
                });
            });
        }

        form.addEventListener("click", function (event) {
            const button = event.target.closest("button[data-participant-action]");
            if (!button || !form.contains(button)) {
                return;
            }
            event.preventDefault();
            const row = button.closest("tr");
            const time = row.querySelector('input[type="datetime-local"]');
            const adding = button.dataset.participantAction === "add";
            if (adding && !time.reportValidity()) {
                return;
            }
            if (adding) {
                if (checkIn) {
                    const participant = { operatorId: row.id, checkInTime: time.value };
                    for (const field of form.querySelectorAll("[data-participation-default]")) {
                        if (!field.reportValidity()) {
                            return;
                        }
                        participant[field.dataset.participationDefault] = field.value;
                    }
                    selected.set(row.id, participant);
                } else {
                    selected.set(row.id, time.value);
                }
            } else {
                selected.delete(row.id);
            }
            time.readOnly = adding;
            button.dataset.participantAction = adding ? "remove" : "add";
            button.textContent = adding ? "Remove" : availableLabel;
            form.querySelector(adding ? "#participant-list tbody" : "#operator-list tbody").appendChild(row);
            updatePayload();
        });
    }

    window.NetlogParticipantSelection = { initialize: initialize };
    document.querySelectorAll("form[data-participant-selection]").forEach(initialize);
}());
