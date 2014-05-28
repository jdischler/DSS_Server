
//------------------------------------------------------------------------------
Ext.define('MyApp.view.CalculatorWidget', {
    extend: 'Ext.window.Window',
    alias: 'widget.calculator',

    title: 'Calculator',
    constrainHeader: true, // keep the header from being dragged out of the app body...otherwise may not be able to close it!
    layout: {
    	type: 'table',
    	columns: 
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
            	itemId: 'input',
				xtype: 'textfield',
			}]
        });

        me.callParent(arguments);
    }
    
});


/*
Ext.onReady(function () {
    var wd = 50,
        pad = 5,
        full = wd + pad;
    var ht = 40,
        full_y = ht + pad;
    
    new Ext.window.Window({
        resizable: true,
        title: 'Calculator',
        layout: 'absolute',
//        width: 280,
        items: [
        { // TOP ROW
            xtype: 'button',
            x: 0,
            y: 0,
            width: wd + full,
            height: ht,
            text: 'c'
        }, {
            xtype: 'button',
            x: 2 * full,
            y: 0,
            width: wd,
            height: ht,
            html: '&larr;'
        },{
            xtype: 'button',
            x: 3 * full,
            y: 0,
            width: wd,
            height: ht,
            html: '&plusmn;'
        }, {
            xtype: 'button',
            x: 4 * full,
            y: 0,
            width: wd,
            height: ht,
            html: '&radic;'
        },
        { // START NUMBER ROWS
            xtype: 'button',
            x: 0,
            y: 1 * full_y,
            width: wd,
            height: ht,
            text: '7',
        }, {
            xtype: 'button',
            x: 1 * full,
            y: 1 * full_y,
            width: wd,
            height: ht,
            text: '8'
        },{
            xtype: 'button',
            x: 2 * full,
            y: 1 * full_y,
            width: wd,
            height: ht,
            text: '9'
        }, {
            xtype: 'button',
            x: 3 * full,
            y: 1 * full_y,
            width: wd,
            height: ht,
            html: '&divide;'
        }, {
            xtype: 'button',
            x: 4 * full,
            y: 1 * full_y,
            width: wd,
            height: ht,
            html: 'x&sup2;'
        },
        { // NUMBER ROWS
            xtype: 'button',
            x: 0,
            y: 2 * full_y,
            width: wd,
            height: ht,
            text: '4',
        }, {
            xtype: 'button',
            x: 1 * full,
            y: 2 * full_y,
            width: wd,
            height: ht,
            text: '5'
        },{
            xtype: 'button',
            x: 2 * full,
            y: 2 * full_y,
            width: wd,
            height: ht,
            text: '6'
        }, {
            xtype: 'button',
            x: 3 * full,
            y: 2 * full_y,
            width: wd,
            height: ht,
            text: '*'
        }, {
            xtype: 'button',
            x: 4 * full,
            y: 2 * full_y,
            width: wd,
            height: ht,
            text: '1/x'
        },
        { // BOTTOM NUM ROWS
            xtype: 'button',
            x: 0,
            y: 3 * full_y,
            width: wd,
            height: ht,
            text: '1',
        }, {
            xtype: 'button',
            x: 1 * full,
            y: 3 * full_y,
            width: wd,
            height: ht,
            text: '2'
        },{
            xtype: 'button',
            x: 2 * full,
            y: 3 * full_y,
            width: wd,
            height: ht,
            text: '3'
        }, {
            xtype: 'button',
            x: 3 * full,
            y: 3 * full_y,
            width: wd,
            height: ht,
            text: '-'
        }, {
            xtype: 'button',
            x: 4 * full,
            y: 3 * full_y,
            width: wd,
            height: ht + full_y,
            text: '='
        },
        { // ZERO ROW
            xtype: 'button',
            x: 0,
            y: 4 * full_y,
            width: wd + full,
            height: ht,
            text: '0',
        }, {
            xtype: 'button',
            x: 2 * full,
            y: 4 * full_y,
            width: wd,
            height: ht,
            text: '.'
        },{
            xtype: 'button',
            x: 3 * full,
            y: 4 * full_y,
            width: wd,
            height: ht,
            text: '+'
        }]
    }).show();
});

css

.x-btn-inner {
    color:#036 !important;
    font-size: 20px !important;
    font-family: 'helvetica' !important;
}
*/
