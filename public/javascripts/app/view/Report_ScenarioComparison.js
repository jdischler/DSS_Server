
var DSS_ScenarioComparison_DEFAULT = -1;

// TODO: actually save the whole scenario in here? Have a field for it but
//	need to figure out how to wire it all up for how and when to get the scenario
//	back out...
//------------------------------------------------------------------------------
var DSS_ScenarioComparisonStore = Ext.create('Ext.data.Store', {
		
    fields: ['Index', 'ScenarioName', 'Scenario'],
    data: {
        items: [{ 
        	Index: DSS_ScenarioComparison_DEFAULT, 
            ScenarioName: 'CURRENT LANDSCAPE',
            Scenario: undefined
        }]
    },
    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'items'
        }
    },
    listeners: {
    	// blah, just force the commit to happen, no reason not to save it right away IMHO
    	update: function(store, record, operation, eOps) {
    		if (operation == Ext.data.Model.EDIT) {
    			store.commitChanges();
    		}
    	}
    }
});

//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_ScenarioComparison', {
    extend: 'Ext.container.Container',
    alias: 'widget.scenariocompare',

//    id: 'DSS_ScenarioComparisonTool',
    
   	requires: [
    	'MyApp.view.Report_ComparisonTypePopup',
   	],
    
 //   height: 34,
//    layout: 'absolute'
	layout: 'vbox',
	dock: 'top',
	
    header: false,
    bodyStyle: {
    	'background-color': '#f4f8ff'
    },
	// NOTE: these strings MUST be synchronized with the server, or else the server will
	//	not know which files to compare. 
	// More specifically, these are the FILE names that the model process would be writing out
	DSS_CompareFiles: ['net_income','ethanol','net_energy','p_loss_epic','soil_loss',
					'soc','nitrous_oxide','pollinator','pest','habitat_index'],

    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
            	xtype: 'report_comparison_popup'
            },{
            	xtype: 'container',
            	id: 'DSS_ScenarioComparisonTool',
            	hidden: true,
            	layout: 'absolute',
            	height: 34,
            	items: [{
					id: 'DSS_ScenarioCompareCombo_1',
					xtype: 'combobox',
					x: 0,
					y: 5,
					width: 230,
					forceSelection: true,
					allowBlank: false,
					fieldLabel: 'Compare',
					labelAlign: 'right',
					labelWidth: 60,
					store: DSS_ScenarioComparisonStore,
					valueField: 'Index',
					displayField: 'ScenarioName',
					value: DSS_ScenarioComparison_DEFAULT,
					queryMode: 'local'
				},{
					xtype: 'button',
					icon: 'app/images/switch_icon.png',
					tooltip: {
						text: 'Swap values'
					},
					x: 232,
					y: 5,
					handler: function(me,evt) {
						var combo1 = Ext.getCmp('DSS_ScenarioCompareCombo_1');
						var combo2 = Ext.getCmp('DSS_ScenarioCompareCombo_2');
						var temp = combo1.getValue();
						combo1.setValue(combo2.getValue());
						combo2.setValue(temp);
					}
				},{
					id: 'DSS_ScenarioCompareCombo_2',
					xtype: 'combobox',
					x: 250,
					y: 5,
					width: 200,
					forceSelection: true,
					allowBlank: false,
					fieldLabel: 'To',
					labelAlign: 'right',
					labelWidth: 30,
					store: DSS_ScenarioComparisonStore,
					valueField: 'Index',
					displayField: 'ScenarioName',
	//				value: DSS_ScenarioComparison_CURRENT,
					queryMode: 'local'
				},
				{
					xtype: 'button',
					x: 455,
					y: 5,
					width: 30,
					text: 'Go',
					handler: function() {
						me.initComparison();
					}
				}]
			}]
        });

        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
    initComparison: function() {
    	
		var self = this;
		var combo1 = Ext.getCmp('DSS_ScenarioCompareCombo_1');
		var combo2 = Ext.getCmp('DSS_ScenarioCompareCombo_2');
		var requestData = {
			clientID: 1234, //temp
			compareCount: this.DSS_CompareFiles.length,
			compare1ID: combo1.getValue(),//-1, // default
			compare2ID: combo2.getValue()
		};
		
		var clientID_cookie = Ext.util.Cookies.get('DSS_clientID');
		if (clientID_cookie) {
			requestData.clientID = clientID_cookie;
		}
		else {
			requestData.clientID = 'BadID';
			console.log('WARNING: no client id cookie was found...');
		}
		
		var button = Ext.getCmp('DSS_runModelButton');
		button.setIcon('app/images/spinner_16a.gif');
		button.setDisabled(true);
		
		var obj = Ext.Ajax.request({
			url: location.href + 'initComparison',
			jsonData: requestData,
			timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
				
				try {
					var obj= JSON.parse(response.responseText);
					console.log("success: ");
					console.log(obj);
					var newRequest = requestData;
					newRequest.customCompareID = obj.customCompareID;
					self.submitComparisons(newRequest);
				}
				catch(err) {
					console.log(err);
				}
			},
			
			failure: function(respose, opts) {
				button.setIcon('app/images/go_icon.png');
				button.setDisabled(false);
				alert("Model run failed, request timed out?");
			}
		});
	},
	
	//--------------------------------------------------------------------------
	submitComparisons: function(newRequest) {
	 
		var button = Ext.getCmp('DSS_runModelButton');
		
		var requestCount = this.DSS_CompareFiles.length;
		var successCount = 0;
		
		Ext.getCmp('DSS_ReportDetail').setWaitFields();
		Ext.getCmp('DSS_SpiderGraphPanel').clearSpiderData(0);// set all fields to zero
		// Disable the save button until all models complete...
		Ext.getCmp('DSS_ScenarioSaveButton').setDisabled(true);

		for (var i = 0; i < this.DSS_CompareFiles.length; i++) {
			var request = newRequest;
			request.file = this.DSS_CompareFiles[i];
			
			var obj = Ext.Ajax.request({
				url: location.href + 'runComparison',
				jsonData: request,
				timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
				
				success: function(response, opts) {
					
					try {
						var obj= JSON.parse(response.responseText);
						console.log("success: ");
						console.log(obj);
						Ext.getCmp('DSS_ReportDetail').setData(obj);
					}
					catch(err) {
						console.log(err);
					}
					// NOTE: most likely is going to be expaned already since this is where the
					//	GO button is?
					var reportPanel = Ext.getCmp('DSS_report_panel');
					if (reportPanel.getCollapsed() != false) {
						reportPanel.expand();
					}
					requestCount--;
					successCount++;
					if (requestCount <= 0) {
						button.setIcon('app/images/go_icon.png');
						button.setDisabled(false);
						
						// Only enable save button if all models succeed?
						if (successCount >= files.length) {
							Ext.getCmp('DSS_ScenarioSaveButton').setDisabled(false);
						}
					}
				},
				
				failure: function(respose, opts) {
					requestCount--;
					if (requestCount <=0) {
						button.setIcon('app/images/go_icon.png');
						button.setDisabled(false);
						alert("Comparison failed, request timed out?");
					}
				}
			});
		}
	}
	
});

