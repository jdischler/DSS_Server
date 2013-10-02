
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_Common', {
    extend: 'Ext.panel.Panel',

    layout: {
        type: 'absolute'
    },
    
    hideCollapseTool: true,
    DSS_noCollapseTool: false,  
    bodyStyle: {
    	'background-color': '#fafcff'
    },
    header: {
    	style: {
    		'background-image': 'none',
    		'background-color': '#ebf2ff !important',
			border: '1px dotted #d0d8e7'
    	}
    },

	//--------------------------------------------------------------------------    
	listeners: {
		afterrender: function(c) { 
			
			// Place the collapse/expand tool at the front if needed, else space it...
/*			// NOTE: always adding a collapse tool for now and disabling if it can't be toggled
			if (0 && c.DSS_noCollapseTool) {
				var spc = Ext.create('Ext.toolbar.Spacer',
				{
					width: 17
				});
				c.header.insert(0,spc);
			}
			else*/ {
				var tool = Ext.create('Ext.panel.Tool', {
					type: (this.collapsed ? 'plus' : 'minus'),
					tooltip: {
						text: 'Show/Hide Query Options',
						showDelay: 100
					},
					toolOwner: c,
					handler: function(evt, toolEl, owner, tool) {
						console.log('Clicked panel tool');
						if (tool.type == 'plus') {
							owner.expand();
						} else {
							owner.collapse();
						}
					}
				});
				c.header.insert(0,tool);
				c.DSS_collapseTool = tool;
			}
			if (c.DSS_noCollapseTool) {
				tool.setDisabled(true);
			}

			// if no layer is bound, don't enable the check options, etc.
			var checkStyle = 'position: relative; top: -2px;';
			var checkDisabled = false;
			var makeChecked = c.DSS_Layer ? c.DSS_Layer.getVisibility() : false;
			// NOTE: This was added to keep Selection layers from being toggled on/off...
			//	maybe not the best idea so commetning it out...
//			if (!c.DSS_Layer) {
//				checkStyle += 'filter: progid:DXImageTransform.Microsoft.Alpha(Opacity=15);' + 
//					'opacity: 0.15;';
//				checkDisabled = true;
//			}
			
			// Layer visiblity check box...		
			var chk = Ext.create('Ext.form.field.Checkbox', {
				itemId: 'DSS_visibilityToggle',
				padding: '0 4 0 4',
				checked: makeChecked,
				disabled: checkDisabled,
				fieldStyle: checkStyle
			});
			
			chk.on({
				'dirtychange': function(me) {
					if (me.getValue() == true) {
						me.DSS_associatedOpacitySlider.show();
						if (c.DSS_ShortTitle && c.DSS_AutoSwapTitles) {
							c.setTitle(c.DSS_ShortTitle);
						}
					}
					else
					{
						me.DSS_associatedOpacitySlider.hide();
						if (c.DSS_LongTitle && c.DSS_AutoSwapTitles) {
							c.setTitle(c.DSS_LongTitle);
						}
					}
					c.DSS_Layer.setVisibility(me.getValue());
				},
				scope: c
			});
			c.header.insert(1,chk);
			
			// opacity slider...
			var hideSlider = (c.DSS_Layer ? !c.DSS_Layer.getVisibility() : true);
			var slider = Ext.create('Ext.slider.Single', {
				itemId: 'DSS_opacitySlider',
				width: 140,
				padding: '0 10 0 10',
				value: 50,
				minValue: 1,
				maxValue: 100,
				increment: 1,
				fieldLabel: 'Opacity',
				labelWidth: 45,
				hidden: hideSlider,
				listeners: {
					change: function(slider, newvalue) {
						c.adjustOpacity(slider);
					},
					scope: c
				}
			});
			chk.DSS_associatedOpacitySlider = slider;
			c.header.insert(3, slider);
			if (c.DSS_ShortTitle) {
				c.DSS_LongTitle = c.title;
				if (hideSlider == false && c.DSS_AutoSwapTitles) {
					c.setTitle(c.DSS_ShortTitle);
				}
			}
			
			// Query button if needed, else space it out...
			if (c.DSS_noQueryTool) {
				var spc = Ext.create('Ext.toolbar.Spacer',
				{
					width: 42
				});
				c.header.add(spc);
			}
			else {
/*				var drop = Ext.create('Ext.button.Button', {
					width: 15,
					arrowAlign: 'left',
					tooltip: {
						text: 'Layer Options',
						showDelay: 100
					},
					menu: {
						// to remove icon 'tray' on left...
						//	though it just removes the bar but still uses all of the space, booo..
						plain: true, 
						items: [{
							xtype: 'slider',
							fieldLabel: 'Layer Opacity',
							indent: false,
							labelWidth: 75,
							labelSeparator: '',
							labelCls: 'x-menu-item-slider-text',
							padding: '3 4 -4 5',
							width: 200
						}]
					}
				});
				c.header.add(drop);
	
				console.log(drop);
				var spc = Ext.create('Ext.toolbar.Spacer',
				{
					width: 4
				});
				c.header.add(spc);
*/				
				var queryButton = Ext.create('Ext.button.Button', {
					itemId: 'DSS_ShouldQuery',
					text: 'Query',
					width: 42,
					height: 20,
					tooltip: {
						text: 'Include this layer in your query?',
						showDelay: 100
					},
					enableToggle: true,
					handler: function(self) {
						if (DSS_DoExpandQueried) {
							if (self.pressed) {
								c.expand();
							}
							else {
								c.collapse();
							}
						}
					}
				});
				c.header.add(queryButton);
			}

			// and one at the end to give space for the scroll bar?
			var spc = Ext.create('Ext.toolbar.Spacer',
			{
				width: 20
			});
			c.header.add(spc);
		},
		expand: function(panel, eOpts) {
			panel.DSS_collapseTool.setType('minus');
		},
		collapse: function(panel, eOpts) {
			panel.DSS_collapseTool.setType('plus');
		}
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
