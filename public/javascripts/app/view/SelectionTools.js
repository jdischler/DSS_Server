
Ext.define('MyApp.view.SelectionTools', {
		
    extend: 'Ext.grid.Panel',
    alias: 'widget.selectiontools',

    collapsed: false,
    title: 'Feature Selection',
    titleAlign: 'center',
    enableColumnHide: false,
    enableColumnMove: false,
    enableColumnResize: false,
    sortableColumns: false,
    store: 'selectionTool',
    
    tools:[{
			type:'help',
			qtip: 'Contract Help',
			handler: function(event, target, owner, tool) {
				var help = Ext.create('MyApp.view.QueryPrototype').show();
			}
    }],

    initComponent: function() {
        var me = this;

        this.combo2Store = Ext.create('Ext.data.SimpleStore', {
        		fields:['id','name'],
        		data:[
							['Corn', 'Corn'],
							['Soy','Soy'],
							['Switchgrass','Switchgrass'],
							['Alfalfa','Alfalfa'],
							['Any Crop','Any Crop'],
							['Roads', 'Roads'],
							['All Urban','All Urban'],
							['Rivers','Rivers'],
							['Lakes','Lakes'],
							['Prairie','Prairie'],
							['Deciduous','Deciduous'],
							['Conifer','Conifer'],
							['All Water', 'All Water'],
							['All Terrestrial', 'All Terrestrial'],
							['All Natural','All Natural']
        		]
        });
        
        this.categoryStore = Ext.create('Ext.data.SimpleStore', {
        		fields: ['type', 'name'],
        		data: [
								['All', 'All Features'],
								['Crops','Crop Types'],
								['Urban','Urban Features'],
								['Natural','Natural Features'],
						]
        });
        
        Ext.applyIf(me, {
            columns: [{
							header: 'Category',
							dataIndex: 'category',
							width: 125,
							editor: new Ext.form.ComboBox({
								valueField: 'type',
								displayField: 'name',
								typeAhead: true,
								triggerAction: 'all',
								selectOnTab: true,
								forceSelection: true,
								allowBlank: false,
								store: this.categoryStore,
								lazyRender: true,
								listClass: 'x-combo-list-small',
							}),
						},
						{
            	id: 'ST_TypeCombo',
							header: 'Type',
							dataIndex: 'type',
							width: 125,
							editor: new Ext.form.ComboBox({
								valueField: 'id',
								displayField: 'name',
								typeAhead: true,
								triggerAction: 'all',
								selectOnTab: true,
								forceSelection: true,
								allowBlank: false,
								store: this.combo2Store,
								lazyRender: true,
								listClass: 'x-combo-list-small',
								listeners: {
									beforeedit: this.onBeforeEdit
								}
							}),
						},
						{
							header: 'Op',
							dataIndex: 'operation',
							width: 53,
							field: {
								xtype: 'combobox',
								typeAhead: true,
								triggerAction: 'all',
								selectOnTab: true,
								forceSelection: true,
								allowBlank: false,
								store: [
									['And','and'],
									['Or','or'],
									['Not','not'],
								],
								lazyRender: true,
								listClass: 'x-combo-list-small'
							}
						},
						{
							xtype: 'actioncolumn',
							width: 96,
							items: [{
								icon: 'app/images/add.png',
								tooltip: 'Insert'
							},
							{
								icon: 'app/images/delete.png',
								tooltip: 'Delete'
							},
							{
								icon: 'app/images/up.png',
								tooltip: 'Move Up'
							},
							{
								icon: 'app/images/down.png',
								tooltip: 'Move Down'
							}]
						}],
            selType: 'cellmodel',
            plugins: [
							Ext.create('Ext.grid.plugin.CellEditing', {
								ptype: 'cellediting',
								clicksToEdit: 1,
								listeners: {
								beforeedit: this.onBeforeEdit
								}
							})
            ],

            dockedItems: [{
							xtype: 'toolbar',
							dock: 'bottom',
							items: [{
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
									icon: 'app/images/add_icon.png',
									scale: 'medium',
									text: 'Add To'
							}]
						}]
        });

        me.callParent(arguments);
    },
    
		onBeforeEdit: function(editor, e, eOpts) {
			var store = Ext.getCmp('ST_TypeCombo').getEditor().getStore();
			
				if (e.record.data.category == 'Crops') {
					store.loadData(
						[
							['Corn', 'Corn'],
							['Soy','Soy'],
							['Switchgrass','Switchgrass'],
							['Alfalfa','Alfalfa'],
							['Any Crop','Any Crop']
						], false);
				}
				else if (e.record.data.category == 'Urban') {
					store.loadData(
						[
							['Roads', 'Roads'],
							['All Urban','All Urban']
						], false);
				}
				else if (e.record.data.category == 'Natural') {
					store.loadData(
						[
							['Rivers','Rivers'],
							['Lakes','Lakes'],
							['Prairie','Prairie'],
							['Deciduous','Deciduous'],
							['Conifer','Conifer'],
							['All Water', 'All Water'],
							['All Terrestrial', 'All Terrestrial'],
							['All Natural','All Natural']
						], false);
				}
				else {
					store.loadData(
						[
							['Corn', 'Corn'],
							['Soy','Soy'],
							['Switchgrass','Switchgrass'],
							['Alfalfa','Alfalfa'],
							['Any Crop','Any Crop'],
							['Roads', 'Roads'],
							['All Urban','All Urban'],
							['Rivers','Rivers'],
							['Lakes','Lakes'],
							['Prairie','Prairie'],
							['Deciduous','Deciduous'],
							['Conifer','Conifer'],
							['All Water', 'All Water'],
							['All Terrestrial', 'All Terrestrial'],
							['All Natural','All Natural']
						], false);
				}
		}

});