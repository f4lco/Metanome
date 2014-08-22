/*
 * Copyright 2014 by the Metanome project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.uni_potsdam.hpi.metanome.frontend.client.datasources;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.DbSystem;
import de.uni_potsdam.hpi.metanome.frontend.client.TabWrapper;
import de.uni_potsdam.hpi.metanome.frontend.client.helpers.InputValidationException;
import de.uni_potsdam.hpi.metanome.frontend.client.input_fields.ListBoxInput;
import de.uni_potsdam.hpi.metanome.frontend.client.services.DatabaseConnectionService;
import de.uni_potsdam.hpi.metanome.frontend.client.services.DatabaseConnectionServiceAsync;
import de.uni_potsdam.hpi.metanome.results_db.DatabaseConnection;

import java.util.Arrays;

/**
 * Input field to configure a database connection.
 */
public class DatabaseConnectionEditForm extends Grid {

  private DatabaseConnectionServiceAsync databaseConnectionService;
  private DatabaseConnectionTab parent;
  private TabWrapper messageReceiver;

  protected TextBox dbUrlTextbox;
  protected TextBox usernameTextbox;
  protected PasswordTextBox passwordTextbox;
  protected ListBoxInput systemListBox;

  public DatabaseConnectionEditForm(DatabaseConnectionTab parent) {
    super(5, 2);

    this.parent = parent;
    this.databaseConnectionService = GWT.create(DatabaseConnectionService.class);

    this.dbUrlTextbox = new TextBox();
    this.setText(0, 0, "Database URL");
    this.setWidget(0, 1, this.dbUrlTextbox);

    this.systemListBox = new ListBoxInput(false);
    this.systemListBox.setValues(Arrays.asList(DbSystem.names()));
    this.setText(1, 0, "Database System");
    this.setWidget(1, 1, this.systemListBox);

    this.usernameTextbox = new TextBox();
    this.setText(2, 0, "User Name");
    this.setWidget(2, 1, this.usernameTextbox);

    this.passwordTextbox = new PasswordTextBox();
    this.setText(3, 0, "Password");
    this.setWidget(3, 1, this.passwordTextbox);

    this.setWidget(4, 1, new Button("Save", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        saveDatabaseConnection();
      }
    }));
  }

  /**
   * Set the url,username and password in the according text boxes.
   * @param url       the url for the database connection
   * @param system    the database system for the database connection
   * @param username  the username for the database connection
   * @param password  the password for the database connection
   */
  public void setValues(String url, String system, String username, String password) {
    this.dbUrlTextbox.setValue(url);
    this.usernameTextbox.setValue(username);
    this.passwordTextbox.setValue(password);
    this.systemListBox.setSelectedValue(system);
  }

  /**
   * Create a database connection with the specified values.
   * @return a database connection
   * @throws InputValidationException if one of the fields is empty
   */
  public DatabaseConnection getValue() throws InputValidationException {
    DatabaseConnection connection = new DatabaseConnection();

    String url = this.dbUrlTextbox.getValue();
    String username = this.usernameTextbox.getValue();
    String password = this.passwordTextbox.getValue();
    String system = this.systemListBox.getSelectedValue();

    if (url.isEmpty() || username.isEmpty() || password.isEmpty() || system.isEmpty())
      throw new InputValidationException("The database url, username and password should all be set!");

    connection
        .setUrl(url)
        .setUsername(username)
        .setPassword(password);
    // TODO set system

    return connection;
  }

  /**
   * Stores the current database connection in the database.
   */
  private void saveDatabaseConnection() {
    try {
      final DatabaseConnection currentConnection = this.getValue();

      this.databaseConnectionService.storeDatabaseConnection(currentConnection, new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable throwable) {
          messageReceiver.addError("Database Connection could not be stored:" + throwable.getMessage());
        }

        @Override
        public void onSuccess(Void aVoid) {
          reset();
          parent.addDatabaseConnectionToTable(currentConnection);
          parent.updateTableInputTab(currentConnection);
        }
      });
    } catch (InputValidationException e) {
      messageReceiver.addError("Database Connection could not be stored: " + e.getMessage());
    }
  }

  /**
   * Reset all values in the url, username and password text boxes.
   */
  public void reset() {
    this.dbUrlTextbox.setText("");
    this.usernameTextbox.setText("");
    this.passwordTextbox.setText("");
    this.systemListBox.reset();
  }

  /**
   * Set the message receiver.
   * @param tab the message receiver tab wrapper
   */
  public void setMessageReceiver(TabWrapper tab) {
    this.messageReceiver = tab;
  }

}
