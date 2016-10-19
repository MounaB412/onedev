pmease.commons.dragdrop = {
	setupDraggable: function(dragSelector, dragData, dragText) {
		$(dragSelector).draggable({
			cursor: 'pointer',
			helper: function(event) {
				return $('<span data-drag="' + dragData + '">' + dragText + '</span>');
			}
		});
	},
	setupDroppable: function(dropSelector, acceptSelector, callback) {
		$(dropSelector).droppable({
			hoverClass: 'drag-over',
			greedy: true,
			tolerance: 'pointer',
			accept: acceptSelector,
			drop: function(event, ui) {
				callback(ui.helper.data("drag"));
			}
		});
	}
};
