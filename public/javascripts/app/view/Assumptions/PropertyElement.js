
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

        var min = this.DSS_elementDefinition.Min,
        	max = this.DSS_elementDefinition.Max;
        if (typeof min === 'undefined') min = Number.NEGATIVE_INFINITY;
        if (typeof max === 'undefined') max = Number.MAX_VALUE;
        
        var stepSize = this.DSS_elementDefinition.StepSize;
        if (typeof stepSize === 'undefined') stepSize = 1;
        
        var unitPostLabelSpacer = 0;
        if (typeof this.DSS_elementDefinition.PostLabel !== 'undefined') {
        	unitPostLabelSpacer = 15;
        }
        
        var label = '<p style="text-align:right">' + this.DSS_elementDefinition.DisplayName + ':</p>';
        
        Ext.applyIf(me, {
            items: [{
				xtype: 'label',
				x: 0,
				y: -7,
				width: 150,
				html: label
			},{
            	itemId: 'valueField',
				xtype: 'numberfield',
				x: 155,
				y: 2,
				width: 85 - unitPostLabelSpacer, // optionally reserve space for a Post label
				hideEmptyLabel: true,
				labelAlign: 'right',
				labelSeparator: '',
				fieldLabel: this.DSS_elementDefinition.PreLabel,
				labelPad: 4, // default is typically 5?
				labelWidth: 10,
				value: this.DSS_elementDefinition.DefaultValue,
				minValue: min,
				maxValue: max,
				step: stepSize
			},
			{
				xtype: 'label',
				x: 230,
				y: 2,
				width: 20,
				hidden: (typeof this.DSS_elementDefinition.PostLabel === 'undefined'),
				text: this.DSS_elementDefinition.PostLabel
			},
			{
				xtype: 'button',
				x: 250,
				y: 2,
				text: '?',
				hidden: (typeof this.DSS_elementDefinition.HelpText === 'undefined'),
				tooltip: {
					text: this.DSS_elementDefinition.HelpText
				}				
			}]
        });

        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
    getValue: function() {

		var result = {};
		var field = this.getComponent('valueField');
		
		// TODO: rename DefaultValue field? Not really accurate?
		result.key = this.DSS_variableKey;
		result.value = field.getValue();
		
		return result;
    }

});

