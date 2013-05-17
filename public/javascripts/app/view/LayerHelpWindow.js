/*
 * File: MyApp/view/LayerHelpWindow.js
 */
	
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerHelpWindow', {
//------------------------------------------------------------------------------

	extend: 'Ext.window.Window',

    height: 300,
    width: 465,
    bodyPadding: '15 25 5 25',
    title: 'Working with Layers',
    modal: true,
    resizable: false,
    layout: {
    	type: 'absolute'
    },

    //--------------------------------------------------------------------------
    initComponent: function() {
   
		var me = this;
		
		var paragraph1 = "A contract is a legally binding agreement between the producer (farmer) and "
			+ "the buyer such that the producer is then 'on the hook' to produce and sell to the buyer "
			+ "the agreed to amount of product..."; 
		
		var paragraph2 = "If the producer is unable to meet the contract production amounts, the producer must then "
			+ "buy enough product at the current spot market price to cover the difference.";
		
		var paragraph3 = "Any excess product grown by the producer is sold at the spot market price.";
	
		Ext.applyIf(me, {
			items: [{
				xtype: 'draw',
				viewbox: false,
				colspan: 1,
				x: 30,
				y: 16,
				height: 80,
				width: 80,
				items: [{
					type: 'image',
					src: 'resources/contract_icon.png',
					width: 80,
					height: 80
				}]
			},
			{
				xtype: 'label',
				x: 128,
				y: 24,
				width: 300,
				height: 100,
				text: paragraph1
			},
			{
				xtype: 'label',
				x: 25,
				y: 112,
				width: 400,
				height: 50,
				text: paragraph2
			},
			{
				xtype: 'label',
				x: 25,
				y: 180,
				width: 400,
				height: 50,
				text: paragraph3
			},
			{
				xtype: 'button',
				text: 'OK',
				x: 350,
				y: 228,
				width: 64,
				height: 28,
				scope: this,
				handler: function() {
					this.close();
				}
			}]
		});

		me.callParent(arguments);
    }

});
