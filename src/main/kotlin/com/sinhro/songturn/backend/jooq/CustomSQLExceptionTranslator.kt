package com.sinhro.songturn.backend.jooq

import org.jooq.ExecuteContext
import org.jooq.impl.DefaultExecuteListener
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
import org.springframework.jdbc.support.SQLExceptionTranslator
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator

/**
 * This class transforms SQLException into a Spring specific
 * DataAccessException. The idea behind this is borrowed from Adam Zell's Gist
 *
 * @author Petri Kainulainen
 * @author Adam Zell
 * @author Lukas Eder
 * @see [http://www.petrikainulainen.net/programming/jooq/using-jooq-with-spring-configuration/](http://www.petrikainulainen.net/programming/jooq/using-jooq-with-spring-configuration/)
 *
 * @see [https://gist.github.com/azell/5655888](https://gist.github.com/azell/5655888)
 */

/**
 * @sinhro
 * Jooq dont catch custom Postgresql exceptions
 *      like from constraints triggers
 * Moreover jooq for some reason get Null exception [when raised Exceptions from sql]
 *      [In class DefaultExecuteContext on line 730]
 *  try to printStackTrace it
 *      [In class DefaultExecuteContext on line 737]
 *  and got NPE
 * This class fixes the problem, by checking is dataAccessException not null
 */
class CustomSQLExceptionTranslator : DefaultExecuteListener() {
    override fun exception(ctx: ExecuteContext) {

        // [#4391] Translate only SQLExceptions
        if (ctx.sqlException() != null) {
            val dialect = ctx.dialect()
            val translator: SQLExceptionTranslator =
                    if (dialect != null)
                        SQLErrorCodeSQLExceptionTranslator(
                                dialect.thirdParty().springDbName()!!)
                    else
                        SQLStateSQLExceptionTranslator()
            val dataAccessException : org.springframework.dao.DataAccessException? =
                    translator.translate("jOOQ", ctx.sql(), ctx.sqlException()!!)
            dataAccessException?.let {
                ctx.exception(it)
            }
        }
    }

    companion object {
        /**
         * Generated UID
         */
        private const val serialVersionUID = -2450323227461061152L
    }
}