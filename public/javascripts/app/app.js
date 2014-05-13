/*
 * File: app.js
 *
 * This file was generated by Sencha Architect version 2.1.0.
 * http://www.sencha.com/products/architect/
 *
 * This file requires use of the Ext JS 4.1.x library, under independent license.
 * License of Sencha Architect does not include license for Ext JS 4.1.x. For more
 * details see http://www.sencha.com/license or contact license@sencha.com.
 *
 * This file will be auto-generated each and everytime you save your project.
 *
 * Do NOT hand edit this file.
 */

Ext.Loader.setConfig({
    enabled: true,
	paths: {
		GeoExt: "http://geoext.github.com/geoext2/src/GeoExt",
		Ext: "http://cdn.sencha.com/ext/gpl/4.2.1/src"
	}
});

// set up some quick tip defaults...
Ext.apply(Ext.tip.QuickTipManager.getQuickTip(), {
	showDelay: 100
});

// FIXME: workaround for quick tips sizing issue...
// as per Stack overflow:
// http://stackoverflow.com/questions/15834689/extjs-4-2-tooltips-not-wide-enough-to-see-contents

Ext.override(Ext.tip.QuickTip, {
	helperElId: 'ext-quicktips-tip-helper',
	initComponent: function ()
	{
		var me = this;

		me.target = me.target || Ext.getDoc();
		me.targets = me.targets || {};
		me.callParent();

		me.on('move', function ()
		{
			var offset = me.hasCls('x-tip-form-invalid') ? 35 : 12, // UGH, what is this <<
				helperEl = Ext.fly(me.helperElId) || Ext.fly(
					Ext.DomHelper.createDom({
						tag: 'div',
						id: me.helperElId,
						style: {
							position: 'absolute',
							left: '-1000px',
							top: '-1000px',
							'font-size': '12px',
							'font-family': 'tahoma, arial, verdana, sans-serif'
						}
					}, Ext.getBody())
				);

			if (me.html && (me.html !== helperEl.getHTML() || me.getWidth() !== (helperEl.dom.clientWidth + offset)))
			{
				helperEl.update(me.html);
				me.setWidth(Ext.Number.constrain(helperEl.dom.clientWidth + offset, me.minWidth, me.maxWidth));
			}
		}, this);
	}
});

Ext.application({
    stores: [
    ],
    views: [
        'MainViewport'
    ],
    autoCreateViewport: true,
    name: 'MyApp'
});

if (!Array.prototype.indexOf) {
    Array.prototype.indexOf = function (searchElement, fromIndex) {
      if ( this === undefined || this === null ) {
        throw new TypeError( '"this" is null or not defined' );
      }

      var length = this.length >>> 0; // Hack to convert object.length to a UInt32

      fromIndex = +fromIndex || 0;

      if (Math.abs(fromIndex) === Infinity) {
        fromIndex = 0;
      }

      if (fromIndex < 0) {
        fromIndex += length;
        if (fromIndex < 0) {
          fromIndex = 0;
        }
      }

      for (;fromIndex < length; fromIndex++) {
        if (this[fromIndex] === searchElement) {
          return fromIndex;
        }
      }

      return -1;
    };
  }
