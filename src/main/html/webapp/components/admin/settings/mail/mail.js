import Control from 'can-control';
import $ from 'jquery';
import bootbox from 'bootbox';

import Settings from 'models/settings';

import template from './mail.stache';
import showErrorDialog from 'helpers/error-dialog';


export default Control.extend({

  "init": function(element, options) {
    var that = this;

    Settings.findOne({},
      function(settings) {
        $(element).html(template({
          settings: settings,
          if_eq: function(a, b) {
            return a === b;
          }
        }));
        that.settings = settings;
        that.updateProviderVisibility();
        $(element).fadeIn();
      });

  },

  '#mail change': function(e) {
    this.settings.attr('mail', e.target.checked);
    this.updateProviderVisibility();
  },

  '#mail-provider change': function(e) {
    this.settings.attr('mailProvider', $(e.target).val());
    this.updateProviderVisibility();
  },

  updateProviderVisibility: function() {
    var mailEnabled = this.settings.attr('mail');
    var provider = this.settings.attr('mailProvider') || 'smtp';
    
    // Show/hide based on provider selection
    if (mailEnabled) {
      if (provider === 'aws-ses' || provider === 'ses') {
        $('#smtp-config').hide();
        $('#aws-ses-config').show();
      } else {
        $('#smtp-config').show();
        $('#aws-ses-config').hide();
      }
    } else {
      $('#smtp-config').hide();
      $('#aws-ses-config').hide();
    }
  },

  'submit': function(form, event) {
    event.preventDefault();

    var provider = this.settings.attr('mailProvider') || 'smtp';
    this.settings.attr('mailProvider', provider);
    
    // SMTP fields
    this.settings.attr('mailSmtp', $(form).find("[name='mail-smtp']").val());
    this.settings.attr('mailPort', $(form).find("[name='mail-port']").val());
    this.settings.attr('mailUser', $(form).find("[name='mail-user']").val());
    this.settings.attr('mailPassword', $(form).find("[name='mail-password']").val());
    this.settings.attr('mailName', $(form).find("[name='mail-name']").val());
    
    // AWS SES fields
    this.settings.attr('mailAwsSesRegion', $(form).find("[name='mail-aws-ses-region']").val());
    this.settings.attr('mailAwsSesFrom', $(form).find("[name='mail-aws-ses-from']").val());
    this.settings.attr('mailAwsSesConfigurationSet', $(form).find("[name='mail-aws-ses-configuration-set']").val());
    
    this.settings.save(function(data) {
      bootbox.alert("E-Mail configuration updated.");
    }, function(response) {
      showErrorDialog("E-Mail configuration not updated", response);
    });


  }

});
