/*
 * File: app/view/ScenarioTools.js
 */

//------------------------------------------------------------------------------
var ClearScenarioGridStore = Ext.create('Ext.data.Store', {
		
    fields: ['Active', 'SelectionName', 'TransformText', 'ManagementText', 'Transform', 'Query'],
    data: {
        items: [{ 
        	Active: true, 
            SelectionName: 'Undefined', 
        	TransformText: 'Undefined',
        	ManagementText: '',
        	Transform: 1,
        	Query: {}
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
 

// Scenario Summary....
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Scenario_Layout', {
		
    extend: 'Ext.grid.Panel',
    alias: 'widget.scenario_layout',

    requires: [
    	'MyApp.view.Assumptions.PropertyWindow',
    	'MyApp.view.TransformPopup'
    ],
  
	autoScroll: true,
 
    id: 'DSS_ScenarioSummary',
    height: 250,
    minHeight: 250,
    maxHeight: 250,
    width: 300,
	dock: 'bottom',
    
	title: 'Scenario Management',
	viewConfig: {
		stripeRows: true
	},
    bodyStyle: {'background-color': '#fafcff'},
	icon: 'app/images/magnify_icon.png',
	
    store: ClearScenarioGridStore,
    enableColumnHide: false,
    enableColumnMove: false,
    sortableColumns: false,
    columnLines: true,
    
	dockedItems: [{
		xtype: 'toolbar',
		dock: 'bottom',
		items: [{
			xtype: 'button',
			icon: 'app/images/new_icon.png',
			scale: 'medium',
			text: 'Reset Scenario',
			tooltip: {
				text: 'Specify any assumptions for this scenario',
				mouseOffset: [15,-40] // make it pop up at a lower Y value than normal (18)
			},
			handler: function(self) {
				Ext.Msg.show({
					 title: 'Confirm New Scenario',
					 msg: 'Are you sure you want to remove this scenario?',
					 buttons: Ext.Msg.YESNO,
					 icon: Ext.Msg.QUESTION,
					 style: {
					 	 'background-color': '#ffffff'
					 },
					 fn: function(btn) {
					 	 if (btn == 'yes') {
							var store =	self.up(). // goes up to the toolbar level...
								up(). // goes up to the panel level....
								getStore(); 
							store.removeAll();
							store.add({
								Active: true, 
								SelectionName: 'Undefined', 
								TransformText: 'Undefined',
								ManagementText: '',
								Transform: 1,
								Query: {}
							});
							var selModel = self.up().up().getSelectionModel();
							selModel.select(0);
					 	 }
					 }
				});
			}
		},
		{
			xtype: 'button',
			icon: 'app/images/add_icon.png',
			scale: 'medium',
			text: 'Add Transform',
			tooltip: {
				text: 'Add a new selection and transformation group to this scenario',
				mouseOffset: [15,-40] // make it pop up at a lower Y value than normal (18)
			},
			handler: function(self) {
				self.up(). // goes to toolbar level...
					up(). // goes to panel level, where the functions are...
					getStore().add({
						Active: true, 
						SelectionName: 'Undefined', 
						TransformText: 'Undefined',
						ManagementText: '',
						Transform: 1,
						Query: {}
				});
			}
		},
		{
			xtype: 'button',
			icon: 'app/images/globe_icon.png',
			scale: 'medium',
			text: 'Set Assumptions',
			tooltip: {
				text: 'Specify any global assumptions for this scenario',
				mouseOffset: [15,-40] // make it pop up at a lower Y value than normal (18)
			},
			handler: function(self) {
				var window = Ext.create('MyApp.view.Assumptions.PropertyWindow');//'MyApp.view.GlobalScenarioPopup');
				var pos = [self.getPosition()[0], self.up().getPosition()[1]];
//				console.log(pos);
				window.show();
				var size = window.getSize();
//				console.log(size);
				// eh, just move it down some relative to the clicked button...
				window.showAt(pos[0],// + self.width,
									(pos[1]) - size.height,
									false);
			}
		},
		{
			xtype: 'button',
			id: 'DSS_runModelButton', // must be unique
			icon: 'app/images/go_icon.png',
			iconAlign: 'right',
			scale: 'medium',
			text: 'Run Models',
			tooltip: {
				text: 'Run the Model using the current scenario',
				mouseOffset: [15,-40] // make it pop up at a lower Y value than normal (18)
			},
			handler: function(self) {
				// Blah, if made changes but didn't change selection on the grid, the results
				//	won't be saved...forcing it here. TODO: consolidate with listener handler code that
				//	does the same thing?
				var chart = this.up().up();
				var selModel = chart.getSelectionModel();
				if (selModel.selected.getCount() > 0) {
					var rec = selModel.getSelection()[0];
					if (rec) {
						var query = DSS_ViewSelectToolbar.buildQuery()
						rec.set('Query', query);
						rec.commit();
					}
				}
				
				chart.prepareModelRequest();
			}
		}]
	}],
	
	plugins: [
		Ext.create('Ext.grid.plugin.CellEditing', {
			clicksToEdit: 2,
			listeners: {
				edit: {
					fn: function(editor, e) {
						// no real need for validation, but if we don't commit the changes,
						//	changed fields will show a red triangle in the corner...
						e.record.commit();
						var dssLeftPanel = Ext.getCmp('DSS_LeftPanel');
						dssLeftPanel.up().DSS_SetTitle(e.record.get('SelectionName'));
					}
				}
			}
		})
	],
	viewConfig: {
		getRowClass: function(record, index) {
			var c = record.get('Active')
			if (c == false) {
				return 'dss-greyed';
			}
		},
		plugins: {
			ptype: 'gridviewdragdrop',
			dragText: 'Drag and drop transforms to reorder them'
		}
	},
	
	listeners: {
		celldblclick: function(me, td, cellIndex, record, tr, rowIndex, e, eOpts) {
			
		/*	if (cellIndex == 3) {
				record.set('Active', !record.get('Active')); // Toggle active field
				record.commit();
			}
			else*/ 
			if (cellIndex == 1) {
				var rectOfClicked = e.target.getBoundingClientRect();
				me.up().showTransformPopup(me, rowIndex, rectOfClicked);
			}
		},
		beforeselect: function(me, record, index, eOpts) {
			
			if (me.selected.getCount() > 0) {
				var oldRecord = me.getSelection()[0];
				if (oldRecord) {
					var query = DSS_ViewSelectToolbar.buildQuery()
					oldRecord.set('Query', query);
					oldRecord.commit();
				}
			}
		},
		select: function(me, record, index, eOpts) {
			var query = record.get('Query');
			DSS_ViewSelectToolbar.setUpSelectionFromQuery(query);
			var dssLeftPanel = Ext.getCmp('DSS_LeftPanel');
			dssLeftPanel.up().DSS_SetTitle(record.get('SelectionName'));

		},
		viewready: function(me, eOpts ) {
			me.getSelectionModel().select(0);
		}
	},
	//--------------------------------------------------------------------------
	columns: {
		items:[{
			dataIndex: 'SelectionName',
			text: 'User-Named Selection',
			width: 170,
			resizable: false,
			editor: {
				xtype: 'textfield',
				allowBlank: false
			},
			tdCls: 'dss-grey-scenario-grid'
		},
		{
			dataIndex: 'TransformText',
			text: 'Transforms & Managment',
			width: 190,
			resizable: false,
			tdCls: 'dss-grey-scenario-grid',
			renderer: function(value, meta, record) {
				meta.tdAttr = 'data-qtip="' + record.get("ManagementText") + '"';
				return value;
			}
		},
		{
			xtype: 'checkcolumn',
			dataIndex: 'Active',
			text: 'Active',
			width: 42,
			resizable: false,
			tdCls: 'dss-grey-scenario-grid'
		},
		{
			xtype: 'actioncolumn',
			width: 23,
			resizable: false,
			icon: 'app/images/eye_icon.png',
			tooltip: 'View selection for this tranform',
			handler: function(grid, rowIndex, colIndex) {
				var record = grid.getStore().getAt(rowIndex);
				grid.getSelectionModel().select([record]); // make record selected to make things less confusing IMO
				var query = record.get('Query');
				if (query) {
					DSS_ViewSelectToolbar.submitQuery(query);
				}
			}
		},
		{
			xtype: 'actioncolumn',
			width: 23,
			resizable: false,
			icon: 'app/images/delete_icon.png',
			tooltip: 'Remove this transform',
			handler: function(grid, rowIndex, colIndex) {
				Ext.Msg.show({
					 title: 'Confirm Transform Delete',
					 msg: 'Are you sure you want to delete this transform?',
					 buttons: Ext.Msg.YESNO,
					 icon: Ext.Msg.QUESTION,
					 fn: function(btn) {
					 	 if (btn == 'yes') {
							var record = grid.getStore().getAt(rowIndex);
							grid.getStore().remove(record);
							record.commit();
							var selModel = grid.getSelectionModel();
							if (selModel.selected.getCount() < 1) {
								selModel.select(0);
							}
					 	 }
					 }
				});
			}
		}]
	},

	//--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
        });

        me.callParent(arguments);
    },

	//--------------------------------------------------------------------------
	showTransformPopup: function(grid,rowIndex, rectOfClicked) {
		
		var record = grid.getStore().getAt(rowIndex);
		var transform = record.get('Transform');
		var window = Ext.create('MyApp.view.TransformPopup', {
			DSS_Transform: {Type: transform},
			listeners: {
				beforedestroy: {
					fn: function(win) {
						if (win.DSS_Transform) {
							record.set('Transform', win.DSS_Transform.Type);
							record.set('TransformText', win.DSS_Transform.Text);
							record.set('ManagementText', win.DSS_Transform.Management);
							record.commit();
						}
					}
				}
			}});
		window.show();
		window.setPosition(rectOfClicked.left,
							(rectOfClicked.top - window.getSize().height),
							false);
	},
	
	//--------------------------------------------------------------------------
	prepareModelRequest: function() {
	
		var scCombo1 = Ext.getCmp('DSS_ScenarioCompareCombo_1').getValue();	
		var haveQuery = false;
		var requestData = {
			clientID: 1234, //temp
			compare1ID: scCombo1,//-1, // default
			assumptions: DSS_AssumptionsAdjustable.Assumptions,
			transforms: []
		};
		
		var clientID_cookie = Ext.util.Cookies.get('DSS_clientID');
		if (clientID_cookie) {
			requestData.clientID = clientID_cookie;
		}
		else {
			requestData.clientID = 'BadID';
			console.log('WARNING: no client id cookie was found...');
		}

		var saveID_cookie = Ext.util.Cookies.get('DSS_nextSaveID');
		if (saveID_cookie) {
			requestData.saveID = saveID_cookie;
		}
		else {
			requestData.saveID = 0;
			console.log('WARNING: no save id cookie was found...');
		}

		DSS_currentModelRunID = requestData.saveID;
		var record = DSS_ScenarioComparisonStore.findRecord('Index', DSS_currentModelRunID);
		if (record) {
			DSS_ScenarioComparisonStore.remove(record);
		}
		
		// Add the new record and select it in the combo box....
		DSS_ScenarioComparisonStore.add({'Index': DSS_currentModelRunID, 'ScenarioName': 'Current Unsaved Result'});
		DSS_ScenarioComparisonStore.commitChanges(); // FIXME: this necessary?
		Ext.getCmp('DSS_ScenarioCompareCombo_2').setValue(DSS_currentModelRunID);

		
		var st = this.getStore();
		for (var idx = 0; idx < st.getCount(); idx++) {
			var rec = st.getAt(idx);
			
			if (rec.get('Active')) {
				var query = rec.get('Query');		
				if (query == null) {
					break;
				}
				
				var landUse = rec.get('Transform');
				if (landUse == null) {
					landUse = 1; // blurf, set to corn....
				}
				
				var transform = {
					queryLayers: query.queryLayers,
					newLandUse: landUse
				};
				requestData.transforms.push(transform);
				haveQuery = true;
			}
		}
		
//		console.log(requestData);
		if (haveQuery) {
			this.createScenario(requestData);
		}
		else {
			alert("No query built - nothing to query");
		}
	},
	
    //--------------------------------------------------------------------------
	createScenario: function(requestData) {
		
		var button = Ext.getCmp('DSS_runModelButton');
		button.setIcon('app/images/spinner_16a.gif');
		button.setDisabled(true);
		
		var self = this;
		var obj = Ext.Ajax.request({
			url: location.href + 'createScenario',
			jsonData: requestData,
			timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
				
				try {
					var obj= JSON.parse(response.responseText);
//					console.log("success: ");
//					console.log(obj);
					var newRequest = requestData;
					newRequest.scenarioID = obj.scenarioID;
					self.submitModel(newRequest);
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
    submitModel: function(queryJson) {
    	
//		console.log(queryJson);
		var button = Ext.getCmp('DSS_runModelButton');
		
		// NOTE: these strings MUST be synchronized with the server, or else the server will
		//	not know which models to run. FIXME: should maybe set this up in a more robust fashion?? How?
//		var modelTypes = ['yield', 'n_p', 'pest_pol', 'soc', 'nitrous', 'habitat_index', 'water_quality'];
		var modelTypes = ['yield', 'pest_pol', 'soc', 'nitrous', 'habitat_index', 
								'water_quality', 'epic_phosphorus'];
		
		var requestCount = modelTypes.length;
		
		Ext.getCmp('DSS_ReportDetail').setWaitFields();
		Ext.getCmp('DSS_SpiderGraphPanel').clearSpiderData(0);// set all fields to zero

		for (var i = 0; i < modelTypes.length; i++) {
			var request = queryJson;
			request.modelType = modelTypes[i];
			
			var obj = Ext.Ajax.request({
				url: location.href + 'modelCluster',
				jsonData: request,
				timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
				
				success: function(response, opts) {
					
					try {
						var obj= JSON.parse(response.responseText);
//						console.log("success: ");
//						console.log(obj);
						Ext.getCmp('DSS_ReportDetail').setData(obj);
					}
					catch(err) {
						console.log(err);
					}
					var reportPanel = Ext.getCmp('DSS_report_panel');
					if (reportPanel.getCollapsed() != false) {
						reportPanel.expand();
					}
					requestCount--;
					if (requestCount <=0 ) {
						button.setIcon('app/images/go_icon.png');
						button.setDisabled(false);
					}
				},
				
				failure: function(respose, opts) {
					requestCount--;
					if (requestCount <=0 ) {
						button.setIcon('app/images/go_icon.png');
						button.setDisabled(false);
					}
					alert("Model run failed, request timed out?");
				}
			});
		}
	}
	
});

