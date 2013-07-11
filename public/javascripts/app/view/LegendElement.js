
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LegendElement', {
    extend: 'Ext.container.Container',
    alias: 'widget.legendelement',

    height: 24,
    width: 178,
    layout: {
        type: 'absolute'
    },

    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
//        var pattern = (me.DSS_LegendElementIndex & 0x3);
        var pattern = (me.index & 0x3);
        var BGcolor = (pattern == 0 || pattern == 1) ? '#ffffff' : '#f7faff';
//        var BGcolor = '#ffffff';
        
        Ext.applyIf(me, {
            style: {
                'background-color': BGcolor,
                border: '1px solid #f7f7f7'
            },
            items: [{
				xtype: 'container',
				x: 5,
				y: 1,
				frame: false,
				height: 19,
				width: 20,
				html: '',
				style: {
					'background-color': me.color,//DSS_LegendElementColor,
					border: '1px dotted #BBBBBB'
				}
			},
			{
				xtype: 'label',
				x: 30,
				y: 2,
				text: me.label,//DSS_LegendElementType
			},
			{
				xtype: 'checkboxfield',
				itemId: 'DSS_queryCheck',
				x: 145,
				y: -1,
				fieldLabel: 'Label',
				hideLabel: true
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
    setChecked: function(shouldBeChecked) {
    	
    	var comp = this.getComponent('DSS_queryCheck');
    	comp.setValue(shouldBeChecked);
    },

    //--------------------------------------------------------------------------
    getElementQueryIndex: function() {
    	
//    	return this.DSS_Index;
    	return this.index;
    }

});

