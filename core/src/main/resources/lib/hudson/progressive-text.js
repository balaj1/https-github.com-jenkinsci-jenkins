Behaviour.specify(
  ".progressiveText-holder",
  "progressive-text",
  0,
  function (holder) {
    let href = holder.getAttribute("data-href");
    let idref = holder.getAttribute("data-idref");
    let spinner = holder.getAttribute("data-spinner");
    let startOffset = holder.getAttribute("data-start-offset");
    let onFinishEvent = holder.getAttribute("data-on-finish-event");

    var scroller = new AutoScroller(document.body);
    /*
  fetches the latest update from the server
  @param e
      DOM node that gets the text appended to
  @param href
      Where to retrieve additional text from
  */
    function fetchNext(e, href, onFinishEvent) {
      var headers = {
        [document.head.dataset.crumbHeader]: document.head.dataset.crumbValue
      };
      if (e.consoleAnnotator !== undefined) {
        headers["X-ConsoleAnnotator"] = e.consoleAnnotator;
      }

      fetch(href, {
        method: 'post',
        headers,
        body: new URLSearchParams({
          start: e.fetchedBytes
        })
      }).then(rsp => {
        if (rsp.status >= 500 || rsp.status === 0) {
          setTimeout(function () {
            fetchNext(e, href, onFinishEvent);
          }, 1000);
          return;
        }
        if (rsp.status === 403) {
          // likely an expired crumb
          location.reload();
          return;
        }
        /* append text and do autoscroll if applicable */
        var stickToBottom = scroller.isSticking();
        rsp.text()
          .then(responseText => {
            var text = responseText;
            if (text !== "") {
              var p = document.createElement("DIV");
              e.appendChild(p); // Needs to be first for IE
              p.innerHTML = text;
              Behaviour.applySubtree(p);
              if (stickToBottom) {
                scroller.scrollToBottom();
              }
            }

            e.fetchedBytes = rsp.headers.get("X-Text-Size");
            e.consoleAnnotator = rsp.headers.get("X-ConsoleAnnotator");
            if (rsp.headers.get("X-More-Data") === "true") {
              setTimeout(function () {
                fetchNext(e, href, onFinishEvent);
              }, 1000);
            } else {
              if (spinner !== "") {
                $(spinner).style.display = "none";
              }
              if (onFinishEvent) {
                Event.fire(window, onFinishEvent);
              }
            }
          })
      })
    }
    $(idref).fetchedBytes = startOffset !== "" ? Number(startOffset) : 0;
    fetchNext($(idref), href, onFinishEvent);
  }
);
