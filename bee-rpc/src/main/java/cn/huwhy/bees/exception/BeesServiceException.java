/*
 *  Copyright 2009-2016 Weibo, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package cn.huwhy.bees.exception;

/**
 * wrapper service exception.
 * 
 * @author maijunsheng
 * 
 */
public class BeesServiceException extends BeesAbstractException {
    private static final long serialVersionUID = -3491276058323309898L;

    public BeesServiceException() {
        super(BeesErrorMsgConstant.SERVICE_DEFAULT_ERROR);
    }

    public BeesServiceException(BeesErrorMsg beesErrorMsg) {
        super(beesErrorMsg);
    }

    public BeesServiceException(String message) {
        super(message, BeesErrorMsgConstant.SERVICE_DEFAULT_ERROR);
    }

    public BeesServiceException(String message, BeesErrorMsg beesErrorMsg) {
        super(message, beesErrorMsg);
    }

    public BeesServiceException(String message, Throwable cause) {
        super(message, cause, BeesErrorMsgConstant.SERVICE_DEFAULT_ERROR);
    }

    public BeesServiceException(String message, Throwable cause, BeesErrorMsg beesErrorMsg) {
        super(message, cause, beesErrorMsg);
    }

    public BeesServiceException(Throwable cause) {
        super(cause, BeesErrorMsgConstant.SERVICE_DEFAULT_ERROR);
    }

    public BeesServiceException(Throwable cause, BeesErrorMsg beesErrorMsg) {
        super(cause, beesErrorMsg);
    }
}
