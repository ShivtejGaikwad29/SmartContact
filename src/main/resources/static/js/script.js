// Inside script.js
$(document).ready(function () {
  console.log("script.js loaded");

  window.togglesidebar = function () {
    if ($(".sidebar").is(":visible")) {
      $(".sidebar").css("display", "none");
      $(".content").css("margin-left", "0%");
    } else {
      $(".sidebar").css("display", "block");
      $(".content").css("margin-left", "20%");
    }
  };
});

const search = () => {
    let query = $("#search-input").val().trim();
  
    if (query === "") {
      $(".search-result").hide();
    } else {
      let url = `http://localhost:8080/search/${encodeURIComponent(query)}`;
  
      fetch(url)
        .then((response) => {
          if (!response.ok) throw new Error("Network error");
          return response.json();
        })
        .then((data) => {
          let text = `<div class='list-group'>`;
  
          data.forEach((contact) => {
            if (contact.name) {
              text += `<a href='/user/${contact.cid}/contact' class='list-group-item list-group-item-action'>
                        ${contact.name}
                      </a>`;
            }
          });
  
          text += `</div>`;
  
          $(".search-result").html(text).show();
        })
        .catch((err) => {
          console.error("Fetch failed:", err);
        });
    }
  };
  
  
  