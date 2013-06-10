/*
 * File: app/view/ScenarioTools.js
 */

 var testGridStore = Ext.create('Ext.data.Store', {
    fields : ['active', 'query', 'transforms'],
    data   : {
        items : [
            { active: true, query: 'Corn and Beans', transforms: 'To grass'  }//,
//            { active: true, query: 'Corn on slopes > 30 near Rivers', transforms: 'To Miscanthus'  },
        ]
    },
    proxy  : {
        type   : 'memory',
        reader : {
            type : 'json',
            root : 'items'
        }
    }
});
 
// Scenario Summary....
Ext.define('MyApp.view.ScenarioTools', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.scenariotools',

    id: 'DSS_ScenarioSummary',
    height: 150,
    minHeight: 150,
    maxHeight: 150,
    width: 300,
    title: 'Scenario Summary',
	viewConfig: {
		stripeRows: true
	},
    tools:[{
		type: 'help',
		qtip: 'Scenario Help',
		handler: function(event, target, owner, tool) {
			var help = Ext.create('MyApp.view.LayerHelpWindow').show();
		}
    }],
    
    enableColumnHide: false,
    enableColumnMove: false,

    store: testGridStore,
 
    bodyStyle: {'background-color': '#fafcff'},
    header: {
    	style: {
    		'background-image': 'none',
    		'background-color': '#ebf2ff !important',
			border: '1px dotted #d0d8e7'
    	},
    	icon: 'app/images/scenario_icon.png'
    },

	//--------------------------------------------------------------------------    
	listeners: {
		afterrender: function(c) { 
			
			var spc = Ext.create('Ext.toolbar.Spacer',
			{
				width: 20
			});
			el = c.header.insert(0,spc);
		}
	},

	dockedItems: [{
		xtype: 'toolbar',
		dock: 'bottom',
		items: [{
			xtype: 'tbspacer', 
			width: 20
		},
		{
			xtype: 'button',
			icon: 'app/images/new_icon.png',
			scale: 'medium',
			text: 'New'
		},
		{
			xtype: 'button',
			icon: 'app/images/save_icon.png',
			scale: 'medium',
			text: 'Save'
		},
		{
			xtype: 'button',
			icon: 'app/images/load_icon.png',
			scale: 'medium',
			text: 'Load'
		},
		{
			xtype: 'button',
			icon: 'app/images/go_icon.png',
			scale: 'medium',
			text: 'Run'
		}]
	}],
	
	columns: {
		items:[{
			dataIndex: null,
			width: 30,
			sortable: false,
			resizable: false
		},
		{
			dataIndex: 'query',
			text: 'Query',
			width: 200,
			resizable: false
		},
		{
			dataIndex: 'transforms',
			text: 'Transforms',
			width: 120,
			resizable: false
		},
		{
			dataIndex: 'active',
			text: 'x',
		//	xtype: 'checkcolumn',
			width: 40,
			resizable: false
		}]
	},

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
        });

        me.callParent(arguments);
    }

});