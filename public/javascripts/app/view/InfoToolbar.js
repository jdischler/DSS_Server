// Bottom informational toolbar

//------------------------------------------------------------------------------
Ext.define('MyApp.view.InfoToolbar', {
		
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.infotoolbar',

	dock: 'bottom',
   
	//--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            layout: {
                padding: 10,
                type: 'hbox'
            },
            items: [{
            	xtype: 'tbtext',
            	id: 'DSS_scale_tag'
			},
			{
				xtype: 'tbseparator'
			},
			{
				xtype: 'tbtext',
				text: 'View Area: 2347 ha'
			},
			{
				xtype: 'tbseparator'
			},
			{
				xtype: 'tbtext',
				text: 'Selected Area: 500 ha'
			}]
        });

        me.callParent(arguments);
    }

});