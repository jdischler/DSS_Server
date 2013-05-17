
Ext.define('MyApp.view.LegendElement', {
    extend: 'Ext.container.Container',
    alias: 'widget.legendelement',

    height: 28,
    width: 200,
    layout: {
        type: 'absolute'
    },

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            style: {
                'background-color': '#f7faff',
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
                        'background-color': '#ff0000',
                        border: '1px dotted #BBBBBB'
                    },
                    width: 25
                },
                {
                    xtype: 'label',
                    x: 55,
                    y: 5,
                    text: 'Corn / Soy'
                },
                {
                    xtype: 'checkboxfield',
                    x: 170,
                    y: 3,
                    fieldLabel: 'Label',
                    hideLabel: true
                }
            ]
        });

        me.callParent(arguments);
    }

});

Ext.define('MyApp.view.LegendTitle', {
    extend: 'Ext.container.Container',
    alias: 'widget.legendtitle',

    height: 21,
    width: 200,
    layout: {
        type: 'absolute'
    },

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            style: {
                'background-color': '#e7e7e7',
                border: '1px solid #d0d0d0'
            },
            items: [
                {
                    xtype: 'label',
                    x: 7,
                    y: 0,
                    style: {
                        'text-align': 'center'
                    },
                    width: 50,
                    text: 'Color'
                },
                {
                    xtype: 'label',
                    x: 55,
                    y: 0,
                    style: {
                        'text-align': 'center'
                    },
                    width: 80,
                    text: 'Type'
                },
                {
                    xtype: 'label',
                    x: 151,
                    y: 0,
                    style: {
                        'text-align': 'center'
                    },
                    width: 50,
                    text: 'Query'
                }
            ]
        });

        me.callParent(arguments);
    }

});

Ext.define('MyApp.view.LegendPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.legendpanel',

    requires: [
        'MyApp.view.LegendTitle',
        'MyApp.view.LegendElement'
    ],

    height: 250,
    width: 400,
    layout: {
        type: 'absolute'
    },
	icon: 'app/images/layers_icon.png',
    
    title: 'Cropland Data',
    titleCollapse: false,
    floatable: false,
    
	listeners: {
		afterrender: function(c) { 
			
			var chk = Ext.create('Ext.form.field.Checkbox',
			{
				padding: '0 5 0 2',
				btip: 'Toggle layer visibility'
			});
			var el = c.header.insert(1,chk);
			chk.on('dirtychange', function(me) {
				if (me.getValue() == true) {
					me.DSS_associatedOpacitySlider.show();
				}
				else
				{
					me.DSS_associatedOpacitySlider.hide();
				}
			});
			
			var spc = Ext.create('Ext.toolbar.Spacer',
			{
				width: 20
			});
			el = c.header.insert(3,spc);
			
			var slider = Ext.create('Ext.slider.Single',
			{
				width: 190,
				padding: '0 20 0 10',
				value: 50,
				minValue: 1,
				maxValue: 100,
				increment: 1,
				fieldLabel: 'Opacity',
				labelWidth: 45,
				hidden: true
			});
			el = c.header.insert(4, slider);
			chk.DSS_associatedOpacitySlider = slider;
		}
	},

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [
                {
                    xtype: 'legendtitle'
                },
                {
                    xtype: 'container',
                    y: 21,
                    style: {
                        border: '1px solid #c0c0c0'
                    },
                    width: 200,
                    autoScroll: true,
                    layout: {
                        align: 'stretch',
                        type: 'vbox'
                    },
                    items: [
                        {
                            xtype: 'legendelement',
                            flex: 1
                        },
                        {
                            xtype: 'legendelement',
                            flex: 1
                        },
                        {
                            xtype: 'legendelement',
                            flex: 1
                        },
                        {
                            xtype: 'legendelement',
                            flex: 1
                        },
                        {
                            xtype: 'legendelement',
                            flex: 1
                        }
                    ]
                },
                {
                    xtype: 'button',
                    x: 220,
                    y: 30,
                    text: 'Set Selection'
                }
            ]
        });

        me.callParent(arguments);
    }

});
