/*
 * Copyright [2016] [solomon qbq]
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

package cn.solomonqbq.multitask;

/**
 * 工作单元
 * @author qinbaoqi
 *
 */
public interface TaskUnit {

	/**
	 * 执行逻辑
	 * @return
	 */
	public void execute();
	
	/**
	 * 返回任务在队列中的执行优先级，如任务1(w1)的优先级是1，任务2(w2)和任务3(w3)的优先级都是2
	 * 那么首先执行w1,完成后并行执行w2和w3,全部完成后返回
	 * @return
	 */
	public int getPriorityOfQueue();
}
