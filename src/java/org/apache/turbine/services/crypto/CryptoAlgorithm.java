package org.apache.turbine.services.crypto;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/**
 * This interface describes the various Crypto Algorithms that are
 * handed out by the Crypto Service.
 *
 * @deprecated Use the Fulcrum Crypto component instead.
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */

public interface CryptoAlgorithm
{
    /**
     * Allows the user to set a salt value whenever the
     * algorithm is used. Setting a new salt should invalidate
     * all internal state of this object.
     * <p>
     * Algorithms that do not use a salt are allowed to ignore
     * this parameter.
     * <p>
     * Algorithms must be able to deal with the null value as salt.
     * They should treat it as "use a random salt".
     *
     * @param salt      The salt value
     *
     */

    void setSeed(String salt);

    /**
     * Performs the actual encryption.
     *
     * @param value       The value to be encrypted
     *
     * @return The encrypted value
     *
     * @throws Exception various errors from the underlying ciphers.
     *                   The caller should catch them and report accordingly.
     *
     */

    String encrypt(String value)
            throws Exception;

    /**
     * Algorithms that perform multiple ciphers get told
     * with setCipher, which cipher to use. This should be
     * called before any other method call.
     *
     * If called after any call to encrypt or setSeed, the
     * CryptoAlgorithm may choose to ignore this or to reset
     * and use the new cipher.
     *
     * If any other call is used before this, the algorithm
     * should use a default cipher and not throw an error.
     *
     * @param cipher    The cipher to use.
     *
     */

    void setCipher(String cipher);

}
