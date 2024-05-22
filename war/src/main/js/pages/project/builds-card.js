import debounce from "lodash/debounce";
import behaviorShim from "@/util/behavior-shim";

// Card/item controls
const buildHistoryPage = document.getElementById("buildHistoryPage");
const pageSearch = buildHistoryPage.querySelector(".jenkins-search");
const pageSearchInput = buildHistoryPage.querySelector("input");
const ajaxUrl = buildHistoryPage.getAttribute("page-ajax");
const card = document.querySelector("#jenkins-builds");
const contents = card.querySelector("#jenkins-build-history");
const container = card.querySelector(".app-builds-container");
const noBuilds = card.querySelector("#no-builds");

// Pagination controls
const paginationControls = document.querySelector("#controls");
const paginationPrevious = document.querySelector("#up");
const paginationNext = document.querySelector("#down");

// Refresh variables
let buildRefreshTimeout;
const updateBuildsRefreshInterval = 5000;

/**
 * Refresh the 'Builds' card
 * @param {QueryParameters}  options
 */
function load(options = {}) {
  /** @type {QueryParameters} */
  const params = Object.assign({}, options, { search: pageSearchInput.value });

  // Avoid fetching if the page isn't active
  if (document.hidden) {
    return;
  }

  fetch(ajaxUrl + toQueryString(params)).then((rsp) => {
    if (rsp.ok) {
      rsp.text().then((responseText) => {
        container.classList.remove("app-builds-container--loading");
        pageSearch.classList.remove("jenkins-search--loading");

        // TODO
        if (responseText.trim() === "") {
          contents.innerHTML = "";
          noBuilds.style.display = "block";
          return;
        }

        // TODO
        contents.innerHTML = responseText;
        noBuilds.style.display = "none";
        behaviorShim.applySubtree(contents);

        // TODO
        const div = document.createElement("div");
        div.innerHTML = responseText;
        const innerChild = div.children[0];
        updateCardControls({
          pageHasUp: innerChild.dataset.pageHasUp === "true",
          pageHasDown: innerChild.dataset.pageHasDown === "true",
          pageEntryNewest: innerChild.dataset.pageEntryNewest,
          pageEntryOldest: innerChild.dataset.pageEntryOldest,
        });
      });
    }
  });
}

/**
 * Shows/hides the card's pagination controls depending on the passed parameter
 * @param {CardControlsOptions}  parameters
 */
function updateCardControls(parameters) {
  paginationControls.classList.toggle(
    "jenkins-!-display-none",
    !parameters.pageHasUp && !parameters.pageHasDown,
  );
  paginationPrevious.classList.toggle(
    "app-builds-container__button--disabled",
    !parameters.pageHasUp,
  );
  paginationNext.classList.toggle(
    "app-builds-container__button--disabled",
    !parameters.pageHasDown,
  );

  // We only want the list to refresh if the user is on the first page of results
  if (!parameters.pageHasUp) {
    createRefreshTimeout();
  } else {
    cancelRefreshTimeout();
  }

  buildHistoryPage.dataset.pageEntryNewest = parameters.pageEntryNewest;
  buildHistoryPage.dataset.pageEntryOldest = parameters.pageEntryOldest;
}

paginationPrevious.addEventListener("click", () => {
  load({ "newer-than": buildHistoryPage.dataset.pageEntryNewest });
});

paginationNext.addEventListener("click", () => {
  cancelRefreshTimeout();
  load({ "older-than": buildHistoryPage.dataset.pageEntryOldest });
});

function createRefreshTimeout() {
  cancelRefreshTimeout();
  buildRefreshTimeout = window.setTimeout(
    () => load(),
    updateBuildsRefreshInterval,
  );
}

function cancelRefreshTimeout() {
  if (buildRefreshTimeout) {
    window.clearTimeout(buildRefreshTimeout);
    buildRefreshTimeout = undefined;
  }
}

const debouncedLoad = debounce(() => {
  load();
}, 150);

document.addEventListener("DOMContentLoaded", function () {
  pageSearchInput.addEventListener("input", function () {
    container.classList.add("app-builds-container--loading");
    pageSearch.classList.add("jenkins-search--loading");
    debouncedLoad();
  });

  load();
});
