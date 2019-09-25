
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LegendElement', {
    extend: 'Ext.container.Container',
    alias: 'widget.legendelement',

    height: 24,
    width: 172,
    layout: {
        type: 'absolute'
    },

    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        var pattern = (this.DSS_LegendElementIndex & 0x3);
        var BGcolor = (pattern == 0 || pattern == 1) ? '#ffffff' : '#f7faff';
        me.DSS_GreyColor = '#ecf3f6';
        me.DSS_LegendElementColor = me.color;
        me.DSS_LegendElementType = me.label;
        
        Ext.applyIf(me, {
            style: {
                'background-color': BGcolor,
                border: '1px solid #eef',
                'border-bottom': '1px solid #cde'
            },
            items: [{
				xtype: 'container',
				itemId: 'DSS_ColorChip',
				x: 5,
				y: 1,
				frame: false,
				height: 19,
				width: 20,
				html: '',
				style: {
//					'background-color': me.DSS_GreyColor,//DSS_LegendElementColor,
					'background-color': me.DSS_LegendElementColor,
					border: '1px dotted #BBBBBB'
				},
				// blah, make the color chip clickable...so it toggles the value of the checkbox on us...
				listeners: {
					render: function (c) {
						c.el.on('click', function () {
							var comp = c.up().getComponent('DSS_queryCheck');
							comp.setValue(!comp.getValue());
							DSS_RefilterDelayed();
						});
					}
				}
			},
			{
				xtype: 'label',
				x: 30,
				y: 2,
				text: me.DSS_LegendElementType
			},
			{
				xtype: 'checkboxfield',
				itemId: 'DSS_queryCheck',
				x: 145,
				y: -1,
				fieldLabel: 'Label',
				hideLabel: true,
				handler: function() {
					DSS_RefilterDelayed();
				}
			}]
        });

        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
    elementIsChecked: function() {
    	
    	var comp = this.getComponent('DSS_queryCheck');
    	return comp.getValue();
    },
    
    //--------------------------------------------------------------------------
    showColorChip: function() {
    	
/*    	var chip = this.getComponent('DSS_ColorChip');
    	chip.el.setStyle({'background-color': this.DSS_LegendElementColor});
*/
    },
    
    //--------------------------------------------------------------------------
    hideColorChip: function() {
    	
/*    	var chip = this.getComponent('DSS_ColorChip');
    	chip.el.setStyle({'background-color': this.DSS_GreyColor});
*/
    },
    
    //--------------------------------------------------------------------------
    setChecked: function(shouldBeChecked) {
    	
    	var comp = this.getComponent('DSS_queryCheck');
    	comp.setValue(shouldBeChecked);
    },

    //--------------------------------------------------------------------------
    getElementQueryIndex: function() {
    	
    	return this.index;
    }

});

