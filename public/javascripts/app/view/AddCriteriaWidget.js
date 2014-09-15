
//------------------------------------------------------------------------------
Ext.define('MyApp.view.AddCriteriaWidget', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.add_criteria',

	hideCollapseTool: true,
	collapsed: true,
	
	listeners: {
		afterrender: function(c) { 
			
			var queryButton = Ext.create('Ext.button.Button', {
				itemId: 'DSS_ShouldQuery',
				text: 'Add',
				width: 55,
				height: 20,
				tooltip: {
					text: c.DSS_Description
				},
				enableToggle: true,
				listeners: {
					toggle: function(self, pressed) {
						if (self.pressed) {
							self.setText('Remove');
//							c.DSS_AssociatedLayer.expand();
//							c.DSS_AssociatedLayer.show();
						}
						else {
							self.setText('Add');
/*							c.DSS_AssociatedLayer.hide();
							c.DSS_AssociatedLayer.tryDisableClickSelection();
							c.DSS_AssociatedLayer.collapse();*/
						}
						c.DSS_AssociatedLayer.header.getComponent('DSS_ShouldQuery').toggle(self.pressed);//tryEnableClickSelection();
					}
				}
			});
			c.header.add(queryButton);
		
			// and one at the end to give space for the scroll bar?
			var spc = Ext.create('Ext.toolbar.Spacer',
			{
				width: 16
			});
			c.header.add(spc);
		}
	}	
});

