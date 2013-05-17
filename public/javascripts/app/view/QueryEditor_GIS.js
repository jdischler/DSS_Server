Ext.define('MyApp.view.QueryEditor_GIS', {
    extend: 'Ext.window.Window',

    height: 591,
    width: 470,
    layout: {
        type: 'absolute'
    },
    modal: true,
    resizable: false,
    title: 'Select By Attributes',

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [
                {
                    xtype: 'textareafield',
                    x: 10,
                    y: 10,
                    height: 20,
                    width: 440,
                    fieldLabel: 'Layer:',
                    labelWidth: 60
                },
                {
                    xtype: 'checkboxfield',
                    x: 76,
                    y: 30,
                    boxLabel: 'Only show selectable layers in this list.'
                },
                {
                    xtype: 'textareafield',
                    x: 10,
                    y: 50,
                    height: 20,
                    width: 440,
                    fieldLabel: 'Method',
                    labelWidth: 60
                },
                {
                    xtype: 'textareafield',
                    x: 10,
                    y: 80,
                    height: 70,
                    width: 440
                },
                {
                    xtype: 'button',
                    x: 10,
                    y: 160,
                    width: 50,
                    scale: 'medium',
                    text: '=',
                    handler: function(button, e) {
                    		var field = button.ownerCt.getComponent('sql_field');
                    	  field.setValue(field.value + '= ');
                    }
                },
                {
                    xtype: 'button',
                    x: 70,
                    y: 160,
                    width: 50,
                    scale: 'medium',
                    text: '< >',
                    handler: function(button, e) {
                    		var field = button.ownerCt.getComponent('sql_field');
                    	  field.setValue(field.value + '<> ');
                    }
                },
                {
                    xtype: 'button',
                    x: 130,
                    y: 160,
                    width: 50,
                    scale: 'medium',
                    text: 'Like',
                    handler: function(button, e) {
                    		var field = button.ownerCt.getComponent('sql_field');
                    	  field.setValue(field.value + 'LIKE ');
                    }
                },
                {
                    xtype: 'button',
                    x: 10,
                    y: 200,
                    width: 50,
                    scale: 'medium',
                    text: '>',
                    handler: function(button, e) {
                    		var field = button.ownerCt.getComponent('sql_field');
                    	  field.setValue(field.value + '> ');
                    }
                },
                {
                    xtype: 'button',
                    x: 70,
                    y: 200,
                    width: 50,
                    scale: 'medium',
                    text: '> =',
                    handler: function(button, e) {
                    		var field = button.ownerCt.getComponent('sql_field');
                    	  field.setValue(field.value + '>= ');
                    }
                },
                {
                    xtype: 'button',
                    x: 130,
                    y: 200,
                    width: 50,
                    scale: 'medium',
                    text: 'And',
                    handler: function(button, e) {
                    		var field = button.ownerCt.getComponent('sql_field');
                    	  field.setValue(field.value + 'AND ');
                    }
                },
                {
                    xtype: 'button',
                    x: 10,
                    y: 240,
                    width: 50,
                    scale: 'medium',
                    text: '<',
                    handler: function(button, e) {
                    		var field = button.ownerCt.getComponent('sql_field');
                    	  field.setValue(field.value + '> ');
                    }
                },
                {
                    xtype: 'button',
                    x: 70,
                    y: 240,
                    width: 50,
                    scale: 'medium',
                    text: '< =',
                    handler: function(button, e) {
                    		var field = button.ownerCt.getComponent('sql_field');
                    	  field.setValue(field.value + '<= ');
                    }
                },
                {
                    xtype: 'button',
                    x: 130,
                    y: 240,
                    width: 50,
                    scale: 'medium',
                    text: 'Or',
                    handler: function(button, e) {
                    		var field = button.ownerCt.getComponent('sql_field');
                    	  field.setValue(field.value + 'OR ');
                    }
                },
                {
                    xtype: 'button',
                    x: 10,
                    y: 280,
                    width: 25,
                    scale: 'medium',
                    text: '_'
                },
                {
                    xtype: 'button',
                    x: 35,
                    y: 280,
                    width: 25,
                    scale: 'medium',
                    text: '%'
                },
                {
                    xtype: 'button',
                    x: 70,
                    y: 280,
                    width: 50,
                    scale: 'medium',
                    text: '( )'
                },
                {
                    xtype: 'button',
                    x: 130,
                    y: 280,
                    width: 50,
                    scale: 'medium',
                    text: 'Not',
                    handler: function(button, e) {
                    		var field = button.ownerCt.getComponent('sql_field');
                    	  field.setValue(field.value + 'NOT ');
                    }
                },
                {
                    xtype: 'button',
                    x: 10,
                    y: 320,
                    width: 50,
                    scale: 'medium',
                    text: 'Is'
                },
                {
                    xtype: 'textareafield',
                    x: 190,
                    y: 160,
                    height: 150,
                    width: 260
                },
                {
                    xtype: 'textareafield',
                    itemId: 'sql_field',
                    x: 10,
                    y: 380,
                    height: 90,
                    width: 440,
                    value: ''
                },
                {
                    xtype: 'button',
                    x: 10,
                    y: 480,
                    width: 80,
                    scale: 'medium',
                    text: 'Clear',
                    handler: this.clearSqlField
                },
                {
                    xtype: 'button',
                    x: 100,
                    y: 480,
                    width: 80,
                    scale: 'medium',
                    text: 'Verify'
                },
                {
                    xtype: 'button',
                    x: 190,
                    y: 480,
                    width: 80,
                    scale: 'medium',
                    text: 'Help'
                },
                {
                    xtype: 'button',
                    x: 280,
                    y: 480,
                    width: 80,
                    scale: 'medium',
                    text: 'Load'
                },
                {
                    xtype: 'button',
                    x: 370,
                    y: 480,
                    width: 80,
                    scale: 'medium',
                    text: 'Save'
                },
                {
                    xtype: 'button',
                    x: 280,
                    y: 520,
                    width: 80,
                    scale: 'medium',
                    text: 'Ok'
                },
                {
                    xtype: 'button',
                    x: 370,
                    y: 520,
                    width: 80,
                    scale: 'medium',
                    text: 'Cancel'
                },
                {
                    xtype: 'label',
                    x: 10,
                    y: 360,
                    text: 'SELECT * FROM table WHERE:'
                }
            ]
        });

        me.callParent(arguments);
    },
    
    clearSqlField: function(button, e) {
    	var field = button.ownerCt.getComponent('sql_field');
    	field.setValue('');
    }

});
