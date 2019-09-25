
//--------------------------------------------------------------------------
Ext.define('MyApp.view.Report_GenerateReport', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.report_generate_report',

    height: 64,
    width: 300,
    title: 'Step 4: Store Results',
//	icon: 'app/images/new_icon.png',
	//collapsed: true,
	
    layout: 'absolute',
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
			items: [{
				xtype: 'button',
				id: 'DSS_ScenarioSaveButton',
				disabled: true,
				text: 'Store Results',
				icon: 'app/images/save_small_icon.png',
				tooltip: {
					text: 'Store these results so they can be compared with your other scenarios'
				},
				x: 30,
				y: 6,
				height: 24,
				handler: function(btn) {
					me.askSaveResults(btn);
				}
			}]
        });

        me.callParent(arguments);
    },

    //--------------------------------------------------------------------------
    askSaveResults: function(save_button) {
    	
    	var me = this;
    	Ext.Msg.prompt('Store As:', 'Please name this scenario:', function(msg_btn, text) {
			if (msg_btn == 'ok') {
				var res = Ext.util.Cookies.get('DSS_nextSaveID');
				res++;
				if (res > 9) {
					res = 0;
				}

				Ext.util.Cookies.set('DSS_nextSaveID', res);

				// clean up old records...
				var record = DSS_ScenarioComparisonStore.findRecord('Index', DSS_currentModelRunID);
				if (record) {
					DSS_ScenarioComparisonStore.remove(record);
				}
				
				// Add the new record and select it in the combo box....
				DSS_ScenarioComparisonStore.add({'Index': DSS_currentModelRunID, 'ScenarioName': text});
				DSS_ScenarioComparisonStore.commitChanges(); // FIXME: this necessary?
				Ext.getCmp('DSS_ScenarioCompareCombo_2').setValue(DSS_currentModelRunID);
				DSS_currentModelRunID = res;
				
				Ext.getCmp('DSS_ScenarioComparisonTool').show();
				
				// Disable button again until model run is pressed...
				save_button.setDisabled(true);
			}
    	});
    }
    
});
