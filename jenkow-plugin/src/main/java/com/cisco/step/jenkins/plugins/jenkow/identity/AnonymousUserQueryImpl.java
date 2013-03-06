/*
 * The MIT License
 * 
 * Copyright (c) 2012, Cisco Systems, Inc., Max Spring
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cisco.step.jenkins.plugins.jenkow.identity;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import hudson.security.SecurityRealm;
import jenkins.model.Jenkins;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.interceptor.CommandContext;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * {@link UserQuery} in case Jenkins doesn't have {@link SecurityRealm}.
 *
 * @author Kohsuke Kawaguchi
 */
class AnonymousUserQueryImpl extends UserQueryImpl {
    private final User ANONYMOUS;

    AnonymousUserQueryImpl() {
        ANONYMOUS = new ImmutableUser(Jenkins.ANONYMOUS.getName(),Jenkins.ANONYMOUS.getName(),"","");
    }

    @Override
    public List<User> executeList(CommandContext _, Page page) {
        return query();
    }

    @Override
    public long executeCount(CommandContext _) {
        return executeList(_,null).size();
    }
    
    public List<User> query() {
        return query(Predicates.and(
                checker(new Function<User, String>() {
                    public String apply(User u) {
                        return u.getId();
                    }
                }, literalAndLike(id, null)),

                checker(new Function<User, String>() {
                    public String apply(User u) {
                        return u.getEmail();
                    }
                }, literalAndLike(email, emailLike)),

                checker(new Function<User, String>() {
                    public String apply(User u) {
                        return u.getFirstName();
                    }
                }, literalAndLike(firstName, firstNameLike)),

                checker(new Function<User, String>() {
                    public String apply(User u) {
                        return u.getLastName();
                    }
                }, literalAndLike(lastName, lastNameLike))));
    }

    private Predicate<User> checker(final Function<User,String> function, final Predicate<String> condition) {
        return new Predicate<User>() {
            public boolean apply(User input) {
                return condition.apply(function.apply(input));
            }
        };
    }

    private Predicate<String> literalAndLike(final String exact, String like) {
        if (exact==null && like==null)      return Predicates.alwaysTrue();

        final Pattern p = (like!=null) ? Pattern.compile(like.replace("%",".*").replace('_','?')) : null;
        return new Predicate<String>() {
            public boolean apply(String input) {
                return (exact != null && exact.equals(input))
                    || (p != null && p.matcher(input).matches());
            }
        };
    }

    private List<User> query(Predicate<? super User> pred) {
        if (pred.apply(ANONYMOUS))
            return Collections.singletonList(ANONYMOUS);
        else
            return Collections.emptyList();
    }
}
