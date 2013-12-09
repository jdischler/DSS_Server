

//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_ViewFooter', {
    extend: 'Ext.container.Container',
    alias: 'widget.layer_footer',

    layout: {
        type: 'hbox'
    },
    
    height: 30,
	padding: 4,
    border: false,
    style: {
    	'background-color': '#fafcff'
    },
    
    
    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        
        Ext.applyIf(me, {
			items: [{
				xtype: 'button',
				margin: '0 0 0 100',
				text: 'Show Layer',
				enableToggle: true,
				handler: function(self) {
					Ext.create('Ext.menu.Menu', {
						width: 160,
						plain: true,
						floating: true,
						items: [{
							xtype: 'button',
							text: 'Hide Layer',
							enableToggle: true,
							pressed: true,
							handler: function(self) {
								self.up().close();
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
								//	me.adjustOpacity(slider);
								},
								scope: me
							}
						}]
					}).show();
				}
			}]
		});
		
        me.callParent(arguments);
    }  
    
});

/*    			var chk = Ext.create('Ext.form.field.Checkbox', {
				itemId: 'DSS_visibilityToggle',
				padding: '0 4 0 4',
				checked: makeChecked,
				disabled: checkDisabled,
				fieldStyle: checkStyle
			});
,
		
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
*/

