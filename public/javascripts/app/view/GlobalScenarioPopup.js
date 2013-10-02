
//------------------------------------------------------------------------------
Ext.define('MyApp.view.GlobalScenarioPopup', {
    extend: 'Ext.window.Window',
    alias: 'widget.globalscenariopopup',

    requires: [
    	'MyApp.view.EconomicAssumptions'
    ],
    
    title: 'Set Global Assumptions',
    modal: true,
    resizable: false,
//    closable: false,
    
	icon: 'app/images/globe_icon.png',
    activeTab: 1,

   	//--------------------------------------------------------------------------    
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
            	xtype: 'tabpanel',
				bodyPadding: '5 30 15 0',
				dockedItems: [{
					xtype: 'toolbar',
					dock: 'bottom',
					items: [{
						xtype: 'button',
						icon: 'app/images/new_icon.png',
						scale: 'medium',
						text: 'Defaults'
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
						xtype: 'tbspacer', 
						width: 78
					},
					{
						xtype: 'button',
						icon: 'app/images/go_icon.png',
						scale: 'medium',
						text: 'Close',
						handler: function(self) {
							self.up(). 	// go up to toolbar level (from the button level)
								up().	// go up to tabpanel level toolbar is in...
								up().	// lastly, go up to the window level that the tabPanel is in
								doClose(); 
						}
					}]
				}],
            	items: [{
					xtype: 'panel',
					title: 'Economic',
					icon: 'app/images/economic_icon.png',
					bodyStyle: {
						'background-image': 'none',
						'background-color': '#fff !important',
					},
					items: [{
						xtype: 'economicassumptions'
					}]
				},
				{
					xtype: 'panel',
					title: 'Climate',
					icon: 'app/images/climate_icon.png',
					items: [{
						xtype: 'economicassumptions'
					}]
				},
				{
					xtype: 'panel',
					title: 'Policies',
					icon: 'app/images/policy_icon.png',
					items: [{
						xtype: 'economicassumptions'
					}]
				}]
			}]
        });

        me.callParent(arguments);
    }

});

