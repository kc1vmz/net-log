(function () {
    "use strict";

    document.addEventListener("change", function (event) {
        const mode = event.target;
        if (!mode.matches('form[data-active-event-create] select[name="startMode"]')) {
            return;
        }
        const form = mode.form;
        const scheduled = mode.value === "PENDING";
        const time = form.querySelector('[name="startTimeStr"]');
        form.querySelector("[data-scheduled-start]").hidden = !scheduled;
        time.disabled = !scheduled;
        time.required = scheduled;
    });
}());
