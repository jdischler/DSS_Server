
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_SpiderHeader', {
    extend: 'Ext.container.Container',
    alias: 'widget.report_spider_header',

    height: 28,
    width: 500,
        layout: {
        type: 'absolute'
    },
	style: {
		'background-color': '#f1f4f6',
		border: '1px solid #edf0f0'
	},

    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;
	
        Ext.applyIf(me, {
            items: [{
				xtype: 'label',
				x: 0,
				y: -6,
//				html: '<p style="text-align:right">ToDo Finish this header block....</p>',
				style: {
					'color': '#777'
				}//,
				//width: 85
			}/*,		
			{
				xtype: 'radiofield',
				x: 94,
				y: 3,
				name: 'spiderStyle',
				boxLabel: 'Def',
				checked: true,
				value: true,
				handler: function(checkbox, checked) {
					if (!checked) {
					}
				}
			},
			{
				xtype: 'radiofield',
				x: 166,
				y: 3,
				name: 'spiderStyle',
				boxLabel: 'Other',
				handler: function(checkbox, checked) {
					if (!checked) {
					}
				}
			}*/]
	    });
        
        me.callParent(arguments);
    }
});

