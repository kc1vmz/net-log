(function () {
    "use strict";

    const storageKey = "netlog-theme";
    const root = document.documentElement;
    const systemPreference = window.matchMedia("(prefers-color-scheme: dark)");
    let savedTheme = null;

    try {
        savedTheme = window.localStorage.getItem(storageKey);
    } catch (error) {
        // The toggle still works when browser storage is unavailable.
    }

    let hasSavedPreference = savedTheme === "light" || savedTheme === "dark";
    root.dataset.theme = hasSavedPreference
        ? savedTheme
        : (systemPreference.matches ? "dark" : "light");

    function updateButton() {
        const button = document.querySelector(".theme-toggle");
        if (!button) {
            return;
        }

        const isDark = root.dataset.theme === "dark";
        const label = isDark ? "Switch to light mode" : "Switch to dark mode";
        button.setAttribute("aria-label", label);
        button.setAttribute("aria-pressed", String(isDark));
        button.title = label;
    }

    document.addEventListener("DOMContentLoaded", function () {
        const button = document.querySelector(".theme-toggle");
        const navigation = document.querySelector(".navbar");

        if (button) {
            updateButton();
            button.hidden = false;
            button.addEventListener("click", function () {
                root.dataset.theme = root.dataset.theme === "dark" ? "light" : "dark";
                hasSavedPreference = true;
                updateButton();

                try {
                    window.localStorage.setItem(storageKey, root.dataset.theme);
                } catch (error) {
                    // Keep the current page usable even if the preference cannot be saved.
                }
            });
        }

        if (navigation) {
            const updateNavigationHeight = function () {
                root.style.setProperty("--navigation-height", navigation.offsetHeight + "px");
            };
            updateNavigationHeight();

            if (typeof ResizeObserver !== "undefined") {
                const observer = new ResizeObserver(updateNavigationHeight);
                observer.observe(navigation);
            } else {
                window.addEventListener("resize", updateNavigationHeight);
            }
        }
    });

    systemPreference.addEventListener("change", function (event) {
        if (!hasSavedPreference) {
            root.dataset.theme = event.matches ? "dark" : "light";
            updateButton();
        }
    });

    window.addEventListener("storage", function (event) {
        if (event.key === storageKey || event.key === null) {
            hasSavedPreference = event.newValue === "light" || event.newValue === "dark";
            root.dataset.theme = hasSavedPreference
                ? event.newValue
                : (systemPreference.matches ? "dark" : "light");
            updateButton();
        }
    });
}());
