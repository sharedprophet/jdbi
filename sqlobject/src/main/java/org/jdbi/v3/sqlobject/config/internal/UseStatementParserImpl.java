/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbi.v3.sqlobject.config.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.rewriter.StatementParser;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jdbi.v3.sqlobject.config.Configurer;
import org.jdbi.v3.sqlobject.config.UseStatementParser;

public class UseStatementParserImpl implements Configurer
{
    @Override
    public void configureForMethod(ConfigRegistry registry, Annotation annotation, Class<?> sqlObjectType, Method method)
    {
        UseStatementParser anno = (UseStatementParser) annotation;
        try {
            final StatementParser parser = instantiate(anno.value(), sqlObjectType, method);
            registry.get(SqlStatements.class).setStatementParser(parser);
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void configureForType(ConfigRegistry registry, Annotation annotation, Class<?> sqlObjectType)
    {
        UseStatementParser anno = (UseStatementParser) annotation;
        try {
            final StatementParser parser = instantiate(anno.value(), sqlObjectType, null);
            registry.get(SqlStatements.class).setStatementParser(parser);
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private StatementParser instantiate(Class<? extends StatementParser> value,
                                          Class<?> sqlObjectType,
                                          Method m) throws Exception
    {
        try {
            Constructor<? extends StatementParser> no_arg = value.getConstructor();
            return no_arg.newInstance();
        }
        catch (NoSuchMethodException e) {
            try {
                Constructor<? extends StatementParser> class_arg = value.getConstructor(Class.class);
                return class_arg.newInstance(sqlObjectType);
            }
            catch (NoSuchMethodException e1) {
                if (m != null) {
                    Constructor<? extends StatementParser> c_m_arg = value.getConstructor(Class.class,
                                                                                            Method.class);
                    return c_m_arg.newInstance(sqlObjectType, m);
                }
                throw new IllegalStateException("Unable to instantiate, no viable constructor " + value.getName());
            }
        }
    }
}
