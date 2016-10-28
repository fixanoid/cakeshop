import 'dashboard-framework/dashboard-core';
import 'dashboard-framework/dashboard-util';
import 'dashboard-framework/dashboard-template';

(function () {
	var sandboxTour = new Tour({
		debug: false,
		//storage: false,
		backdrop: true,
		container: "body",
		backdropContainer: "body",
		onEnd: function() {
			//for autostart
			window.localStorage.setItem('sandboxTourEnded', true);
		},
		steps: [
		  {
		    element: "#my-element",
		    title: "Title of my step",
		    content: "Content of my step"
		  },
		  {
		    element: "#my-other-element",
		    title: "Title of my step",
		    content: "Content of my step"
		  }
	  	]
	});

	function loadWidget(tab, widget, click_sel) {
		return function() {
			return new Promise(function(resolve, reject) {
				showMenuStep(tab)();
				if ($(".widget-shell." + widget).length !== 0) {
					return resolve();
				}
				// load it
				$(document).on("WidgetInternalEvent", function(e, action) {
					if (action === "widget|rendered|" + widget) {
						resolve();
						$(document).off(e);
					}
				});
				$(click_sel).click();
			});
		};
	}

	function showMenuStep(id) {
		return function() {
			return new Promise(function(resolve, reject) {
				if (!$(id).hasClass("active")) {
					$(id).click();
				}
				$(".tower-navigation").css({"z-index": 1100});
				$(".tower-sidebar").css({"z-index": 1100});
				resolve();
			});
		};
	}

	function hideMenuStep() {
		$(".tower-navigation").css({"z-index": 10000});
		$(".tower-sidebar").css({"z-index": 9999});
	}

	// Initialize the tour
	sandboxTour.init();

	var sandboxTourLoaded = false;
	// $(document).on("WidgetInternalEvent", function(e, action) {
	$(document).on("StartSandboxTour", function(e, action) {
		//if (action === "node-status|announce" && loaded === false) {
		console.log('start sandboxTour')
		Tower.sandboxTour = sandboxTour;
		window.localStorage.setItem("sandbox_tour_current_step", 0); // always reset to 0
		sandboxTour.start();
		sandboxTourLoaded = true;

		//}
	});

})();
