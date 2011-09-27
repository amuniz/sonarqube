 #
 # Sonar, entreprise quality control tool.
 # Copyright (C) 2008-2011 SonarSource
 # mailto:contact AT sonarsource DOT com
 #
 # Sonar is free software; you can redistribute it and/or
 # modify it under the terms of the GNU Lesser General Public
 # License as published by the Free Software Foundation; either
 # version 3 of the License, or (at your option) any later version.
 #
 # Sonar is distributed in the hope that it will be useful,
 # but WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 # Lesser General Public License for more details.
 #
 # You should have received a copy of the GNU Lesser General Public
 # License along with Sonar; if not, write to the Free Software
 # Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 #
class SetupController < ApplicationController
  skip_before_filter :check_database_version, :check_authentication

  SECTION=Navigation::SECTION_CONFIGURATION
  
  # GETs should be safe (see http://www.w3.org/2001/tag/doc/whenToUseGet.html)
  verify :method => :post, :only => [ :setup_database ], :redirect_to => { :action => :index }
    
  def index
    if DatabaseVersion.uptodate?
      render :template => 'setup/index'
    elsif ActiveRecord::Base.connected?
      render :template => (DatabaseVersion.upgradable? ? 'setup/form' : 'setup/not_upgradable'), :layout => 'nonav'
    else 
      render :template => 'setup/dbdown', :layout => 'nonav'
    end
  end

  def maintenance
    render :template => 'setup/maintenance', :layout => 'nonav'
  end

  def setup_database
    if !DatabaseVersion.upgradable?
      render :text => 'Upgrade is not supported. Please use a production-ready database.', :status => 500
    else
      # do not forget that this code is also in /api/server/setup (see api/server_controller.rb)
      DatabaseVersion.upgrade_and_start unless DatabaseVersion.uptodate?
      redirect_to home_path
    end
  end
end
