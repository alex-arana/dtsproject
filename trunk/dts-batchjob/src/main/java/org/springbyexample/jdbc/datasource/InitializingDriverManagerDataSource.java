   /*
    * Copyright 2002-2006 the original author or authors.
    *
    * Licensed under the Apache License, Version 2.0 (the "License");
    * you may not use this file except in compliance with the License.
    * You may obtain a copy of the License at
    *
    *      http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */

  package org.springbyexample.jdbc.datasource;

  //import java.sql.Driver;

  import org.springbyexample.jdbc.core.SqlScriptProcessor;
  import org.springframework.beans.factory.InitializingBean;
  import org.springframework.jdbc.datasource.SimpleDriverDataSource;
  //import org.springframework.util.Assert;
  import org.springframework.util.ClassUtils;
  import org.springframework.util.StringUtils;


  /**
   * Initializing version of <code>DriverManagerDataSource</code>.
   * After the properties are set any database initialization scripts are run.
   * This is very useful for unit testing.
   *
   * <p>Sample Spring Configuration:</p>
   * 
   * <pre>
   *
   *    <bean id="dataSource" class="org.springbyexample.jdbc.datasource.InitializingDriverManagerDataSource">
   *      <property name="driverClassName" value="org.hsqldb.jdbcDriver"/>
   *      <property name="url" value="jdbc:hsqldb:mem:Test"/>
   *      <property name="username" value="sa"/>
   *      <property name="password" value=""/>
   *
   *      <property name="sqlScriptProcessor">
   *        <bean class="org.springbyexample.jdbc.core.SqlScriptProcessor">
   *         <property name="sqlScripts">
   *             <list>
   *                 <value>classpath:/schema-hsqldb.sql</value>
   *             </list>
   *         </property>
   *        </bean>
   *      </property>
   *   </bean>
   *
   * </pre>
   *
   * @author David Winterfeldt
   *
   * @see org.springframework.jdbc.datasource.SimpleDriverDataSource
   * @see org.springbyexample.jdbc.core.SqlScriptProcessor
   */
  public class InitializingDriverManagerDataSource extends SimpleDriverDataSource
          implements InitializingBean {

      protected String driverClassName = null;
      protected SqlScriptProcessor sqlScriptProcessor = null;

      /**
       * Sets driver class name.  The class should implement
       * <code>java.sql.Driver</code>.  This is a shortcut for
       * calling <code>setDriver(Driver driver)</code> on the parent class.
       */
      public void setDriverClassName(String driverClassName) {
          this.driverClassName = driverClassName;
      }

      /**
       * Sets SQL script processor.
       */
      public void setSqlScriptProcessor(SqlScriptProcessor sqlScriptProcessor) {
          this.sqlScriptProcessor = sqlScriptProcessor;
      }

      /**
       * Implementation of <code>InitializingBean</code>
       */
      public void afterPropertiesSet() throws Exception {
          // if driver class name isn't blank, set
          // driver class on SimpleDriverDataSource.
          if (getDriver() == null && StringUtils.hasText(driverClassName)) {
              setDriverClass(ClassUtils.forName(driverClassName));
          }

          if (sqlScriptProcessor != null) {
              sqlScriptProcessor.setDataSource(this);

              sqlScriptProcessor.process();
          }
      }

  }

