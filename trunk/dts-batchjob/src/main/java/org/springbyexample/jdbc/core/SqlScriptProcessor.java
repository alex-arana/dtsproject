

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

  package org.springbyexample.jdbc.core;

  import java.io.BufferedReader;
  import java.io.IOException;
  import java.io.InputStreamReader;
  import java.util.List;

  import javax.sql.DataSource;

  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.springframework.beans.factory.InitializingBean;
  import org.springframework.context.ResourceLoaderAware;
  import org.springframework.core.io.Resource;
  import org.springframework.core.io.ResourceLoader;
  import org.springframework.jdbc.core.JdbcTemplate;
  import org.springframework.util.StringUtils;

  /**
   * Processes sql scripts.  A list of scripts
   * or script locations can be set.  Spring <code>ResourceLoader</code>
   * is used so valid scripts locations are classpath, file, http, ftp. etc.
   *
   * @author David Winterfeldt
   */
  public class SqlScriptProcessor implements InitializingBean, ResourceLoaderAware {

      final Logger logger = LoggerFactory.getLogger(SqlScriptProcessor.class);

      protected JdbcTemplate template = null;
      protected ResourceLoader resourceLoader = null;

      protected boolean initOnStartup = false;
      protected List<String> lSqlScripts = null;
      protected String charset = null;

      /**
       * Sets <code>DataSource</code>.
       */
      public void setDataSource(DataSource dataSource) {
          template = new JdbcTemplate(dataSource);
      }

      /**
       * Whether or not SQL scripts on startup.
       * Default is <code>false</code>.
       */
      public boolean isInitOnStartup() {
          return initOnStartup;
      }

      /**
       * Sets whether or not SQL scripts run on statup.
       * Default is <code>false</code>.
       */
      public void setInitOnStartup(boolean initOnStartup) {
          this.initOnStartup = initOnStartup;
      }

      /**
       * Gets SQL scripts.
       */
      public List<String> getSqlScripts() {
          return lSqlScripts;
      }

      /**
       * Sets SQL scripts.
       */
      public void setSqlScripts(List<String> lSqlScripts) {
          this.lSqlScripts = lSqlScripts;
      }

      /**
       * Gets charset used to process a sql script file (ex: 'UTF-8').
       * If not set, default character set of the JVM is used.
       */
      public String getCharset() {
          return charset;
      }

      /**
       * Sets charset used to process a sql script file (ex: 'UTF-8').
      * If not set, default character set of the JVM is used.
      */
     public void setCharset(String charset) {
         this.charset = charset;
     }

     /**
      * Implementation of <code>IntializingBean</code>.
      */
     public void afterPropertiesSet() throws Exception {
         if (initOnStartup) {
             process();
         }
     }

     /**
      * Implementation of <code>ResourceLoaderAware</code>.
      */
     public void setResourceLoader(ResourceLoader resourceLoader) {
         this.resourceLoader = resourceLoader;
     }

     /**
      * Initializes SQL scripts.
      * @throws IOException
      */
     public void process() throws IOException {
         if (lSqlScripts != null) {
            for (String sqlScript : lSqlScripts) {
                 String sql =  null;

                 Resource resource = resourceLoader.getResource(sqlScript);

                 if (!resource.exists()) {
                     sql = sqlScript;
                 } else {
                     BufferedReader br = null;

                     try {
                         if (charset == null) {
                             br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
                         } else {
                             br = new BufferedReader(new InputStreamReader(resource.getInputStream(), charset));
                         }

                         StringBuilder sb = new StringBuilder();
                         String line = null;

                         while ((line = br.readLine()) != null) {
                             sb.append(line);
                             sb.append("\n");
                         }

                         sql = sb.toString();
                     } finally {
                         try { br.close(); } catch (Exception e) {}
                    }
                 }

                if (StringUtils.hasLength(sql)) {
                     logger.debug("Initializing db with given sql");
                     // execute sql
                     template.execute(sql);
                 }
             }
        }
    }

}

