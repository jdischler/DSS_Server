/*
 * File: app/view/ScenarioTools.js
 */

//------------------------------------------------------------------------------
var ScenarioGridStore = Ext.create('Ext.data.Store', {
		
    fields: ['Active', 'SelectionName', 'TransformText', 'ManagementText', 'Transform', 'Query'],
    data: {
        items: [{ 
        	Active: true, 
            SelectionName: 'Unstable Crops', 
        	TransformText: 'To Perennial Grass',
        	ManagementText: '<b><i>Management Options:</i></b></br>None',
        	Transform: 9,
        	Query: {
        		clientID: 0,
        		queryLayers: [{
					name: 'rotation',
					type: 'indexed',
					matchValues: [1,2,3,5]
        		},
        		{
        			greaterThanTest: '>=',
        			greaterThanValue: 3,
        			lessThanTest: '<=',
        			lessThanValue: null,
        			name: 'slope',
        			type: 'continuous'
        		},
        		{
        			greaterThanTest: '>=',
        			greaterThanValue: null,
        			lessThanTest: '<=',
        			lessThanValue: 500,
        			name: 'rivers',
        			type: 'continuous'
        		}]
        	}
        }, {
        	Active: false, 
            SelectionName: 'Stable Grasses', 
        	TransformText: 'To Continuous Corn',
        	Transform: 1,
        	Query:  {
        		clientID: 0,
        		queryLayers: [{
					name: 'rotation',
					type: 'indexed',
					matchValues: [8,9]
        		},
        		{
        			greaterThanTest: '>=',
        			greaterThanValue: null,
        			lessThanTest: '<',
        			lessThanValue: 4,
        			name: 'slope',
        			type: 'continuous'
        		},
        		{
        			greaterThanTest: '>=',
        			greaterThanValue: 500,
        			lessThanTest: '<=',
        			lessThanValue: null,
        			name: 'rivers',
        			type: 'continuous'
        		}]
        	}
        }]
    },
    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'items'
        }
    }
});
 
// Scenario Summary....
//------------------------------------------------------------------------------
Ext.define('MyApp.view.ScenarioTools', {
		
    extend: 'Ext.grid.Panel',
    alias: 'widget.scenariotools',

    requires: [
//    	'MyApp.view.GlobalScenarioPopup',
    	'MyApp.view.Assumptions.PropertyWindow',
    	'MyApp.view.TransformPopup'
    ],
    
    id: 'DSS_ScenarioSummary',
    height: 180,
    minHeight: 180,
    maxHeight: 180,
    width: 300,
	dock: 'bottom',
    
//    header: true,
    title: 'Scenario Management',
	viewConfig: {
		stripeRows: true
	},
    store: ScenarioGridStore,
    
    enableColumnHide: false,
    enableColumnMove: false,
    sortableColumns: false,
    
    bodyStyle: {'background-color': '#fafcff'},
	icon: 'app/images/magnify_icon.png',
    
	dockedItems: [{
		xtype: 'toolbar',
		dock: 'bottom',
		items: [{
			xtype: 'button',
			icon: 'app/images/new_icon.png',
			scale: 'medium',
			text: 'New',
			disabled: true
		},
		{
			xtype: 'button',
			icon: 'app/images/save_icon.png',
			scale: 'medium',
			text: 'Save',
			disabled: true
		},
		{
			xtype: 'button',
			icon: 'app/images/load_icon.png',
			scale: 'medium',
			text: 'Load',
			disabled: true
		},
		{
			xtype: 'button',
			icon: 'app/images/globe_icon.png',
			scale: 'medium',
			text: 'Global Assumptions',
			tooltip: {
				text: 'Specify any global assumptions for this scenario',
				showDelay: 100,
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
			scale: 'medium',
			text: 'Run',
			tooltip: {
				text: 'Run the Model using the current scenario',
				showDelay: 100,
				mouseOffset: [15,-40] // make it pop up at a lower Y value than normal (18)
			},
			handler: function(self) {
				this.up().up().buildModel();
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
		}
	},
	
	listeners: {
		celldblclick: function(me, td, cellIndex, record, tr, rowIndex, e, eOpts) {
			
			if (cellIndex == 3) {
				record.set('Active', !record.get('Active')); // Toggle active field
				record.commit();
			}
			else if (cellIndex == 1) {
				me.up().showTransformPopup(me, rowIndex);
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
			console.log('Calling into select...setting up a query');
			var query = record.get('Query');
			console.log(query);
			DSS_ViewSelectToolbar.setUpSelectionFromQuery(query);
			var dssLeftPanel = Ext.getCmp('DSS_LeftPanel');
			dssLeftPanel.up().DSS_SetTitle(record.get('SelectionName'));

		}
	},
	//--------------------------------------------------------------------------
	columns: {
		items:[{
			dataIndex: 'SelectionName',
			text: 'Selection',
			width: 115,
			resizable: false,
			editor: {
				xtype: 'textfield',
				allowBlank: false
			},
			tdCls: 'dss-grey-scenario-grid'
		},
		{
			dataIndex: 'TransformText',
			text: 'Transforms To & Managment Options',
			width: 220,
			resizable: false,
			tdCls: 'dss-grey-scenario-grid',
			renderer: function(value, meta, record) {
				meta.tdAttr = 'data-qtip="' + record.get("ManagementText") + '"';
				return value;
			}
		},
		{
			xtype: 'actioncolumn',
			width: 20,
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
			dataIndex: 'Active',
			text: 'Active',
		//	xtype: 'checkcolumn',
			width: 43,
			resizable: false,
			tdCls: 'dss-grey-scenario-grid'
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
	showTransformPopup: function(grid,rowIndex) {
		
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
		var pos = grid.getPosition(true);
		console.log(pos);
		window.show();
		// eh, just move it down some relative to the rowIndex clicked...
		window.setPosition(pos[0] + grid.width,
							(pos[1] - window.getSize().height),
							false);
	},
	
	//--------------------------------------------------------------------------
	buildModel: function() {
	
		var requestData = {
			clientID: 12345, //temp
			transforms: []
		};
		
		var landUse = this.getStore().getAt(0).data.Transform;
		if (landUse == null) {
			landUse = 1; // blurf, set to corn....
		}
		
		var transform = {
			queryLayers: [],
			newLandUse: landUse
		};
		
		var haveQuery = false;
		for (var i = 0; i < DSS_globalQueryableLayers.length; i++) {
			
			if (DSS_globalQueryableLayers[i].includeInQuery()) {
				var queryComp = DSS_globalQueryableLayers[i].getSelectionCriteria();
				transform.queryLayers.push(queryComp);
				haveQuery = true;
			}
		}
	
		requestData.transforms.push(transform);
		
		console.log(requestData);
		if (haveQuery) {
			this.submitModel(requestData);
		}
		else {
			alert("No query built - nothing to query");
		}
	},
	
	// TODO: use this variant instead
	//--------------------------------------------------------------------------
	buildModel_NEW: function() {
	
		var haveQuery = false;
		var requestData = {
			clientID: 12345, //temp
			transforms: []
		};
		
		var st = this.getStore();
		for (var idx = 0; idx < st.getCount(); idx++) {
			var rec = st.getAt(idx);
			
			if (rec.get('Active')) {
				var query = rec.get('Query');		
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
		
		console.log(requestData);
		if (haveQuery) {
			this.submitModel(requestData);
		}
		else {
			alert("No query built - nothing to query");
		}
	},
	
    //--------------------------------------------------------------------------
    submitModel: function(queryJson) {
    	
		var button = Ext.getCmp('DSS_runModelButton');
		button.setIcon('app/images/spinner_16a.gif');
		button.setDisabled(true);

		var obj = Ext.Ajax.request({
			url: location.href + 'models',
			jsonData: queryJson,
			timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
				
				var obj = JSON.parse(response.responseText);
				console.log("success: ");
				console.log(obj);
				
				Ext.getCmp('Model_Graph').SetData(obj);
				button.setIcon('app/images/go_icon.png');
				button.setDisabled(false);
				
				var reportPanel = Ext.getCmp('DSS_report_panel');
				if (reportPanel.getCollapsed() != false) {
					reportPanel.expand();
				}
			},
			
			failure: function(respose, opts) {
				button.setIcon('app/images/go_icon.png');
				button.setDisabled(false);
				alert("Model run failed, request timed out?");
			}
		});
	}

});

