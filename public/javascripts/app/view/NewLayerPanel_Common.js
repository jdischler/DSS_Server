
//------------------------------------------------------------------------------
Ext.define('MyApp.view.NewLayerPanel_Common', {
    extend: 'Ext.panel.Panel',

    layout: {
        type: 'absolute'
    },
    
    bodyStyle: {
    	'background-color': '#fafcff'
    },
//	icon: 'app/images/null_icon.png',
    header: {
    	style: {
    		'background-image': 'none',
    		'background-color': '#ebf2ff !important',
			'border-width': '1px',
			'border-style': 'solid none none none',
			'border-color': '#c4d0e0',
			'color': '#fff important'
    	}
    },
    hideCollapseTool: true,
    
	//--------------------------------------------------------------------------    
	listeners: {
		afterrender: function(c) { 
			
			// Query button if needed, else space it out...
			if (c.DSS_noQueryTool) {
				var spc = Ext.create('Ext.toolbar.Spacer',
				{
					width: 42
				});
				c.header.add(spc);
			}
			else {
				var queryButton = Ext.create('Ext.button.Button', {
					itemId: 'DSS_ShouldQuery',
//					icon: 'app/images/plus_icon.png',
					text: 'Add',
	//				textAlign: 'center',
//					padding: '0 0 0 9',
					width: 55,
					height: 20,
					tooltip: {
						text: 'Include aspects of this data in your landscape selection?'
					},
					enableToggle: true,
					listeners: {
						toggle: function(self, pressed) {
							if (self.pressed) {
								c.expand();
//								self.setIcon('app/images/minus_icon.png');
								self.setText('Remove');
//								c.header.setIcon('app/images/active_block_icon.png');
								console.log(c.header);
							}
							else {
								c.collapse();
								self.setText('Add');
//								self.setIcon('app/images/plus_icon.png');
//								c.header.setIcon('app/images/block_icon.png');
								console.log(c.header);
							}
						}
					}
				});
				c.header.add(queryButton);
			}

			// and one at the end to give space for the scroll bar?
			var spc = Ext.create('Ext.toolbar.Spacer',
			{
				width: 16
			});
			c.header.add(spc);
		}
	},
	
	// closefunc and scope are optional
    //--------------------------------------------------------------------------
	createOpacityPopup: function(tool, closefunc, scope) {
		
		var me = this;
		
		var rect = tool.getBox();
		
		me.DSS_Layer.setVisibility(true);
		Ext.create('Ext.menu.Menu', {
			width: 160,
			plain: true,
			floating: true,
			listeners: {
				close: function(menu) {
					me.DSS_Layer.setVisibility(false);
					if (closefunc) {
						closefunc.call(scope);
					}
				},
				beforeHide: function(comp) {
					// don't actually hide, just close it proper...
					comp.close();
					return false;
				}
			},
			items: [{
				xtype: 'button',
				text: 'Hide Layer Overlay',
				enableToggle: true,
				pressed: true,
				handler: function(button) {
					button.up().close();
				}
			},{
				xtype: 'slider',
				width: 140,
				padding: 10,
				value: 50,
				minValue: 0,
				maxValue: 100,
				increment: 10,
				fieldLabel: 'Opacity',
				labelWidth: 45,
				listeners: {
					change: function(slider, newvalue) {
						me.adjustOpacity(slider);
					},
					scope: me
				}
			}]
		}).showAt(rect.left - 8, rect.top);
	},

    //--------------------------------------------------------------------------
    adjustOpacity: function(slider) {
    	
    	var value = slider.getValue() / 100.0;

		if (value < 0.01) value = 0.01;
		else if (value > 0.9999) value = 0.99999; // blugh, value of 1 is more transparent than 0.99??
		
    	this.DSS_Layer.setOpacity(value);
    },
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;
        
        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
    includeInQuery: function() {
    
    	var queryToggleButton = this.header.getComponent('DSS_ShouldQuery');
    	return queryToggleButton.pressed;
    }
	
});
