/*
 * File: app/view/ReportTools.js
 */

Ext.define('MyApp.view.Report_GenerateReport', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.report_generate_report',

    height: 64,
    width: 300,
    title: 'Save/Print Results',
	icon: 'app/images/new_icon.png',
	collapsed: true,
	 
    layout: 'absolute',
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
			items: [{
				xtype: 'button',
				disabled: true,
				text: 'Save Results',
				icon: 'app/images/save_small_icon.png',
				x: 100,
				y: 6,
				height: 24,
				handler: function() {
					me.askSaveResults();
				}
			},{
				xtype: 'button',
				disabled: true,
				text: 'Print Results',
				icon: 'app/images/print_small_icon.png',
				x: 250,
				y: 6,
				height: 24,
			}]
        });

        me.callParent(arguments);
    },

    //--------------------------------------------------------------------------
    askSaveResults: function() {
    	
    	var me = this;
    	Ext.Msg.prompt('Save As:', 'Please name this scenario:', function(btn, text) {
			if (btn == 'ok') {
				//me.saveScenarioResults(text);
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
			}
    	});
    }/*,
    
    //--------------------------------------------------------------------------
    saveScenarioResults: function(name) {
  
		var requestData = {
			clientID: 1234, //temp
			name: name
		};
		
		var clientID_cookie = Ext.util.Cookies.get('DSS_clientID');
		if (clientID_cookie) {
			requestData.clientID = clientID_cookie;
		}
		else {
			console.log('WARNING: no client id cookie was found...');
		}
    	
//		var button = Ext.getCmp('DSS_runModelButton');
//		button.setIcon('app/images/spinner_16a.gif');
//		button.setDisabled(true);
		
		var self = this;
		var obj = Ext.Ajax.request({
			url: location.href + 'saveScenario',
			jsonData: requestData,
			timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
				
				try {
					var obj= JSON.parse(response.responseText);
					console.log('success: ');
					console.log(obj);
					
					if (obj.saveSlot) {
						// TODO: 1) find the CurrentScenario data item, 2) remove it
//						console.log(DSS_ScenarioComparisonStore.findRecord('Index', 0));

						// 3) add the new item
						console.log('Attempting to add a new Save slot item for the combo...');
						DSS_ScenarioComparisonStore.add({'Index': obj.saveSlot, 'ScenarioName': name});
						DSS_ScenarioComparisonStore.commitChanges(); // FIXME: this necessary?
						// TODO: 4) set the combo box to select this new item....
					}
					if (obj.removedSlot) {
						console.log('Server says a slot was deleted. Its number is: ' + obj.removedSlot);
						// TODO: 1) find old slot, 2) remove it, 
						//	3) if it was selected in the combo box, select a new item? Anything else?
						DSS_ScenarioComparisonStore.findRecord('Index', obj.removedSlot);
					}
					
				}
				catch(err) {
					console.log(err);
				}
			},
			
			failure: function(respose, opts) {
//				button.setIcon('app/images/go_icon.png');
//				button.setDisabled(false);
				alert("Scenario save failed, request timed out?");
			}
		});
    }
*/
});