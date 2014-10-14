
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_CalculatorPopUp', {
    extend: 'Ext.window.Window',

//    height: 120,
    width: 450,
    title: 'My Window',
	icon: 'app/images/calculator_16.png',
    layout: 'vbox',
    constrainHeader: true, // keep the header from being dragged out of the app body...otherwise may not be able to close it!
    maximizable: false,
    resizable: false,

	defaults: {
		xtype: 'container',
		height: 35,
		width: 420,
		layout: 'absolute',
	},

	//--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;
        
        var baseWidget = [{
			/* creates a container using the defaults object configured as above */
			items: [{
				itemId: 'DSS_ValueField',  
				xtype: 'textfield',
				x: 10,
				y: 5,
				width: 240,
				fieldLabel: me.DSS_Label,
				labelWidth: 100,
				labelAlign: 'right',
				readOnly: true,
				value: me.DSS_initialValue
			},{
				xtype: 'label',
				itemId: 'DSS_UnitsLabel',
				x: 252,
				y: 9,
				text: me.DSS_UnitLabel,
				style: {
					color: '#888'
				}
			}]
		}];
		// Use the calculator def to create an config any conversion widgets specified...
		var itemsDef = me.supplyConversionWidgets(baseWidget);
		
		// Bammmo, create the customized converter window....
        Ext.applyIf(me, {
            items: itemsDef
        });

        me.callParent(arguments);
    },
    
    // Does slighlty more than just multiply...validates inputs as numeric...
    //	handles formatting decimal places, etc
    //--------------------------------------------------------------------------
    multiply: function(val1, val2, extraFactor, preLabel, postLabel) {
    	
		if (Ext.isNumeric(val1) && Ext.isNumeric(val2)) {
			
			var result = val1 * val2;
			
			if (extraFactor && Ext.isNumeric(extraFactor)) {
				result *= extraFactor;
			}
			var places = 1;
			if (Math.abs(result) < 100) places++;
			if (Math.abs(result) < 25) places++;
			if (Math.abs(result) < 10) places++;
			if (Math.abs(result) <= 1) places++;
			
			return '= ' + preLabel + result.toFixed(places) + ' ' + postLabel;
		}
		
		return '= not a number';
    },
    
    //--------------------------------------------------------------------------
    supplyConversionWidgets: function(itemObj) {
    	
    	var me = this;

		for(var i in me.DSS_calculators) {
			var calcDef = me.DSS_calculators[i];
			console.log('Have a calcDef');
			console.log(calcDef);
    		var calculatedValue = me.multiply(me.DSS_initialValue,
    				calcDef.DSS_ConversionFactor,
    				calcDef.DSS_ExtraFactor, // can be null, undefined....
    				calcDef.DSS_ResultsPreUnits,
    				calcDef.DSS_ResultsPostUnits);
    		
    		var height = 35;
    		var labelWidget = {
				itemId: 'DSS_ConversionFactor',  
				xtype: 'textfield',
				x: 10,
				y: 0,
				width: 240,
				fieldLabel: calcDef.DSS_ConversionLabel,
				labelWidth: 140,
				labelAlign: 'right',
				enableKeyEvents: true,
				value: calcDef.DSS_ConversionFactor,
				listeners: {
					keyup: function(field) {
						
						var multiplier = field.getValue();
						var output = field.up().getComponent('DSS_UnitsLabel');
						var text = me.multiply(me.DSS_initialValue,
								multiplier,
								calcDef.DSS_ExtraFactor, // can be null, undefined....
								calcDef.DSS_ResultsPreUnits,
								calcDef.DSS_ResultsPostUnits);
						output.update(text);
					}
				}
			};

			if (calcDef.DSS_HiddenConversion) {
				labelWidget = {
					xtype: 'label',
					x: 10,
					y: -8,
					width: 230,
					html: '<p style="text-align:right">' + calcDef.DSS_ConversionLabel + '</p>',
				}
				height = 28; // these can be much shorter....
			}
			
			var widget = {
				height: height,
				/* creates a container using the defaults object configured as above */
				items: [
					labelWidget,
				{
					xtype: 'label',
					itemId: 'DSS_UnitsLabel',
					x: 252,
					y: 4,
					html: calculatedValue,
					style: {
						color: '#888'
					}
				}]
			};
			
			itemObj.push(widget);
		}
		
    	return itemObj;
    }
    
});


