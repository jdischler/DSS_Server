Ext.define('MyApp.view.QueryPanelTool_v2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.query_panel_v2',

    requires: [
        'MyApp.view.CDL_Panel',
        'MyApp.view.Slope_Panel'
    ],

    width: 350,
    layout: {
        type: 'accordion'
    },
    title: 'Data Layers',

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
				xtype: 'cdl_panel'
			},
			{
				xtype: 'slope_panel'
			}]
        });

        me.callParent(arguments);
    }

});
