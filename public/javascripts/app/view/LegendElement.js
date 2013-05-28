
Ext.define('MyApp.view.LegendElement', {
    extend: 'Ext.container.Container',
    alias: 'widget.legendelement',

    height: 27,
    width: 220,
    layout: {
        type: 'absolute'
    },

    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        var BGcolor = (me.DSS_LegendElementIndex & 0x1) ? '#ffffff' : '#f0f7ff';
        
        Ext.applyIf(me, {
            style: {
                'background-color': BGcolor,
                border: '1px solid #f7f7f7'
            },
            items: [
                {
                    xtype: 'container',
                    x: 20,
                    y: 1,
                    frame: false,
                    height: 22,
                    html: '',
                    style: {
                        'background-color': me.DSS_LegendElementColor,
                        border: '1px dotted #BBBBBB'
                    },
                    width: 25
                },
                {
                    xtype: 'label',
                    x: 55,
                    y: 5,
                    text: me.DSS_LegendElementType
                },
                {
                    xtype: 'checkboxfield',
                    itemId: 'DSS_queryCheck',
                    x: 180,
                    y: 3,
                    fieldLabel: 'Label',
                    hideLabel: true
                }
            ]
        });

        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
    elementIsChecked: function() {
    	
    	var comp = this.getComponent('DSS_queryCheck');
    	return comp.getValue();
    },
    
    //--------------------------------------------------------------------------
    clearCheck: function() {
    	
    	var comp = this.getComponent('DSS_queryCheck');
    	comp.setValue(false);
    },

    
    //--------------------------------------------------------------------------
    getElementQueryIndex: function() {
    	
    	return this.DSS_Index;
    }

});

