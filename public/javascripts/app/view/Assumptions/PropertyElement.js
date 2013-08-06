
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Assumptions.PropertyElement', {
    extend: 'Ext.container.Container',
    alias: 'widget.propertyelement',

    height: 28,
    layout: {
        type: 'absolute'
    },

    // DSS_elementDefinition.Category, .VariableName, .DisplayName, .DefaultValue
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
				xtype: 'numberfield',
				x: 0,
				y: 2,
				width: 245,
				fieldLabel: this.DSS_elementDefinition.DisplayName,
				labelAlign: 'right',
				labelWidth: 160,
				value: this.DSS_elementDefinition.DefaultValue
			},
			{
				xtype: 'button',
				x: 250,
				y: 2,
				text: '?'
			}]
        });

        me.callParent(arguments);
    }

});

