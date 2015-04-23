
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Access_Popup', {
    extend: 'Ext.window.Window',

    requires: [
    ],
    
    width: 1024,
    maxWidth: 2048,
    minWidth: 240,
    height: 640,
    minHeight: 200,
    
    layout: 'fit',
    
    maximizable: true,
    autoScroll: true,
 
	constrainHeader: true, // keep the header from being dragged out of the app body...otherwise may not be able to close it!
    closable: true,
    modal: true,
	icon: 'app/images/layers_icon.png',
    title: 'Administrative tools',

    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
        });

        me.callParent(arguments);
        me.doDataLoad();
    },
    
    // TODO: fixme....
    //--------------------------------------------------------------------------
    doDataLoad: function() {
    
		var self = this;
		self.setDisabled(true);
		self.FW_AjaxRequestObject = Ext.Ajax.request({
			url: location.href + 'get_access',
			jsonData: {},
			timeout: 60 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
				var obj= JSON.parse(response.responseText);
				console.log(obj);
				self.prepareGrid(obj);
				self.setDisabled(false);
			},
			
			failure: function(respose, opts) {
				self.setDisabled(false);
			}
		});
    },
    
    //--------------------------------------------------------------------------
    // dbTable is expected to come from the server like this:
    //
    //	dbTable. 					-- entire packaged result from the server
    //		.definition[]			-- an array of bit flag names (string) that correspond to access switches
    //		.data[].				-- an array of data elements, one for each registered user
    //			.email				-- email address of the given user
    //			.organization		-- optional organization name
    //				.flags[]		-- array of bit switches, size of this should match the size of the definition[] above
    //			
    //--------------------------------------------------------------------------
    prepareGrid: function(dbTable) {
    
    	// Build the custom columns for the grid panel...we have a few base items always there...
		var customColumns = [{
			text: 'User',
			width: 225,
			sortable: true,
			dataIndex: 'email'
		},{
			text: 'Organization',
			width: 200,
			sortable: true,
			dataIndex: 'organization'
		},{
			xtype: 'checkcolumn',
			text: 'IsAdmin?',
			width: 90,
			sortable: true,
			dataIndex: 'admin',
			disabled: true // NOTE: enabling this won't allow admin rights changes - the server code to allow this change is disabled!
		}];
	
		// ...but then add as many switches as needed...
		for (var i = 0; i < dbTable.definition.length; i++) {
			var newItem = {
				xtype: 'checkcolumn',
				text: dbTable.definition[i] + '?',
				width: 80,
				sortable: true,
				dataIndex: dbTable.definition[i]
			};
			
			customColumns.push(newItem);
		}
		
		// We need a custom data store to drive the grid panel....again, add the base items
		var customFields = [
				{name: 'email',			type: 'string'},
				{name: 'organization',	type: 'string'},
				{name: 'admin',			type: 'boolean'}];
		// ...then add custom fields to finish
		for (var i = 0; i < dbTable.definition.length; i++) {
			var newItem = {
				name: dbTable.definition[i],
				type: 'boolean'
			};
			
			customFields.push(newItem);
		}
		
		// Next, create a custom data store that matches our dynamic definition...
		var customStore = Ext.create('Ext.data.Store', {
			fields: customFields,
			proxy: {
				type: 'memory'
			}
		});
	
		// ...then populate it with the data that we've got
		for (var i = 0; i < dbTable.data.length; i++) {
			// base fixed data...
			var entry = {
				email: dbTable.data[i].email, 
				organization: dbTable.data[i].organization,
				admin: dbTable.data[i].admin
			}; 
			// dynamic flags....
			for (var t = 0; t < dbTable.definition.length; t++) {
				entry[dbTable.definition[t]] = dbTable.data[i].flags[t];
			}
			console.log(entry);
			customStore.add(entry);
		}
		customStore.commitChanges(); // ensure the store thinks it is up to date...

		// Lastly make a new grid panel to tie everything together...
		var panel = Ext.create('MyApp.view.Access_Panel', {
			store: customStore,
			columns: customColumns
		});
		
		this.add(panel);
	}
	
});


//------------------------------------------------------------------------------
Ext.define('MyApp.view.Access_Panel', {
    extend: 'Ext.grid.Panel',

    layout: 'fit',
//    title: 'User access control',
//	icon: 'app/images/globe_icon.png',
	header: false,
	
    enableColumnHide: false,
    enableColumnMove: false,
    sortableColumns: true,
    columnLines: true,
    
 //   store: usersStore,
    
	tbar: [{
		xtype: 'button', 
		text: 'Save changes',
		icon: 'app/images/save_icon.png',
		scale: 'medium',
		handler: function(btn) {
			btn.up().up().submitChanges(btn);
		}
	},{
		xtype: 'button', 
		text: 'Cancel',
		icon: 'app/images/revert_icon.png',
		scale: 'medium',
		handler: function(btn) {
			btn.up().up().up().close();
		}
	}],

	viewConfig: {
		enableTextSelection: true
	},
	
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: []
        });

        me.callParent(arguments);
    },

    //--------------------------------------------------------------------------
	submitChanges: function(button) {
		
		var me = this;
		var store = me.getStore();
		
		var changes = store.getModifiedRecords();
		var sendableChanges = [];
		for (var i=0; i < changes.length; i++) {
			sendableChanges.push(changes[i].data);
		}
		
		console.log(sendableChanges);
	
		var oldIcon = button.icon;
		button.setIcon('app/images/wait_26.gif');
		
		me.setDisabled(true);

		me.FW_AjaxRequestObject = Ext.Ajax.request({
			url: location.href + 'change_access',
			jsonData: sendableChanges,
			timeout: 60 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
				store.commitChanges();
//				usersStore.removeAll();
//				self.populateEdits();
				button.setIcon(oldIcon);
				me.setDisabled(false);
			},
			
			failure: function(respose, opts) {
				me.setDisabled(false);
				button.setIcon(oldIcon);
				Ext.Msg.alert('Save failed', 'Saving the changes failed. Try saving again. (though multiple save failures likely indicate a server issue)');
			}
		});
	}
    
});

