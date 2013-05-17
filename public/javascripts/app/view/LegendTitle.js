
Ext.define('MyApp.view.LegendTitle', {
    extend: 'Ext.container.Container',
    alias: 'widget.legendtitle',

    height: 21,
    width: 220,
    layout: {
        type: 'absolute'
    },

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            style: {
                'background-color': '#e7e7e7',
                border: '1px solid #b0b0d0'
            },
            items: [
                {
                    xtype: 'label',
                    x: 7,
                    y: 1,
                    width: 50,
                    style: {
                        'text-align': 'center',
                        'font-weight': 'bold',
                        'color': '#005'
                    },
                    text: 'Color'
                },
                {
                    xtype: 'label',
                    x: 55,
                    y: 1,
                    width: 50,
                    style: {
                        'text-align': 'left',
                        'font-weight': 'bold',
                        'color': '#005'
                    },
                    text: 'Type'
                },
                {
                    xtype: 'label',
                    x: 161,
                    y: 1,
                    width: 50,
                    style: {
                        'text-align': 'center',
                        'font-weight': 'bold',
                        'color': '#005'
                    },
                    text: 'Query'
                }
            ]
        });

        me.callParent(arguments);
    }

});

