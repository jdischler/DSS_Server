
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Login_Popup', {
    extend: 'Ext.window.Window',

    requires: [
    	'MyApp.view.Login_RegisterPopup',
    	'MyApp.view.Login_ForgotPopup'
    ],
    
    height: 150,
    width: 350,
    title: 'Login',
	icon: 'app/images/tool.png',
    
    layout: 'absolute',
    constrainHeader: true, // keep the header from being dragged out of the app body...otherwise may not be able to close it!
    maximizable: false,
    resizable: false,
    modal: true,

	//--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;
        
        Ext.applyIf(me, {
            items: [{
            	itemId: 'DSS_LoginUser',  
				xtype: 'textfield',
				x: 10,
				y: 15,
				width: 300,
				fieldLabel: 'User Email',
				labelWidth: 100,
				labelAlign: 'right',
				emptyText: 'user',
				vtype: 'email',
				validateBlank: true,
				allowBlank: false
			},{
            	itemId: 'DSS_LoginPWD1',  
				xtype: 'textfield',
				x: 10,
				y: 45,
				width: 300,
				fieldLabel: 'Password',
				labelWidth: 100,
				labelAlign: 'right',
				inputType: 'password',
				emptyText: 'password',
				allowBlank: false
			},{
				xtype: 'button',
				x: 40, 
				y: 80,
				width: 80,
				text: 'Login',
				tooltip: {
					text: "Already registered? Type your email account used for registration and the password, then click here"
				},
				handler: function() {
					me.tryLogin();
				}
			},{
				xtype: 'button',
				x: 130, 
				y: 80,
				width: 80,
				text: 'Forgot',
				tooltip: {
					text: "Forgot your password? Click here to start the process of getting a new password"
				},
				handler: function() {
					Ext.create('MyApp.view.Login_ForgotPopup').show();
					me.close();
				}
			},{
				xtype: 'button',
				x: 220, 
				y: 80,
				width: 80,
				text: 'Register',
				tooltip: {
					text: "Want access to restricted features? Consider registering to begin the process of accessing those features"
				},
				handler: function() {
					Ext.create('MyApp.view.Login_RegisterPopup').show();
					me.close();
				}
			}]
        });

        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
    tryLogin: function(button) {
    
    	var me = this;
		var user = me.getComponent('DSS_LoginUser').getValue();
		var pwd = me.getComponent('DSS_LoginPWD1').getValue();
    	
		// TODO: better email validation...for now, length of 6 because 'a@b.co' is the shortest legal email address
		if (!user || user.length < 6) {
			Ext.MessageBox.alert('Register Error', 'The user field cannot be left empty and must be a valid email address!');
			return;
		}
		else if (!pwd || pwd.length < 1) {
			Ext.MessageBox.alert('Register Error', 'The password field cannot be left empty!');
			return;
		}
		
		var requestData = {
			user: user,
			pwd: pwd
		};
		
		var obj = Ext.Ajax.request({
			url: location.href + 'login',
			jsonData: requestData,
			timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
			
				var obj= JSON.parse(response.responseText);

				for (var t = 0; t < obj.restrictedLayers.length; t++) {
					var restricted = obj.restrictedLayers[t];
					
					console.log(restricted);
					var found = false;
					for (var i = 0; i < obj.unrestrictedLayers.length; i++) {
						if (obj.unrestrictedLayers[i] == restricted) {
							found = true;
							break;
						}
					}
					
					for (var i = 0; i < DSS_globalQueryableLayers.length; i++) {
						var layer = DSS_globalQueryableLayers[i];
						if (layer.DSS_QueryTable == restricted) {
							if (!found) {
								layer.DSS_AccessLocked = true;
								layer.hide();
							}
							else {
								layer.DSS_AccessLocked = false;
							}
						}
					}
				}
				me.DSS_LoginButton.setText('Logout');
				me.DSS_LoginButton.DSS_LoggedIn = true;
				if (obj.showExtra == true) {
					Ext.getCmp('DSS_LogoPanel').showExtra();
				}
				me.close();
			},
			
			failure: function(response, opts) {
				Ext.MessageBox.alert('Login Error', response.responseText);
			}
		});
	}
    
});


