/*
 *  Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.google.androidstudiopoet.models

import com.google.gson.Gson
import java.security.InvalidParameterException

class ConfigPOJO {

    // name of the new project
    lateinit var projectName: String

    // directory where generator should put generated project
    lateinit var root: String

    // how many modules
    var numModules: Int = 0

    // how many methods should be generated all together (!!!)
    var allMethods: String? = null

    // how many java methods should be generated all together
    var javaMethodCount: String? = null

    // how many java packages should be generated
    var javaPackageCount: String? = null

    // how many kotlin packages should be generated
    var kotlinPackageCount: String? = null

    // how many classes should be generated in each Java packages
    var javaClassCount: String? = null

    // how many classes should be generated in each Kotlin packages
    var kotlinClassCount: String? = null

    val javaMethodsPerClass: Int
        get() {
            val totalJavaClasses = (Integer.parseInt(javaClassCount!!) * Integer.parseInt(javaPackageCount!!))
            return if (totalJavaClasses >0 ) Integer.parseInt(javaMethodCount!!) / totalJavaClasses else 0
        }

    private val allKotlinMethods: Int
        get() = Integer.parseInt(allMethods!!) - Integer.parseInt(javaMethodCount!!)

    val kotlinMethodsPerClass: Int
        get() {
            val totalKotlinClasses = Integer.parseInt(kotlinClassCount!!) * Integer.parseInt(kotlinPackageCount!!)
            return if (totalKotlinClasses > 0)  allKotlinMethods / totalKotlinClasses else 0
        }

    var dependencies: List<DependencyConfig>? = null

    val androidModules: String? = null

    val numActivitiesPerAndroidModule: String? = null

    val productFlavors: List<Int>? = null

    val gradleVersion: String? = ""

    val kotlinVersion: String? = null

    val androidGradlePluginVersion: String? = null

    val topologies: List<Map<String, String>>? = null

    override fun toString(): String = toJson()

    private fun toJson(): String {
        val gson = Gson()

        return gson.toJson(this)
    }

    val useKotlin: Boolean
        get() = kotlinPackageCount!!.toInt() > 0


    val resolvedDependencies: Map<Int, Set<DependencyConfig>> by lazy {

        val allDependencies: MutableMap<Int, MutableSet<DependencyConfig>> = mutableMapOf()
        // Add dependencies generated by topologies
        val givenTopologies = topologies
        if (givenTopologies != null) {
            for (parameters in givenTopologies) {
                val type = parameters.get("type") ?: throw InvalidParameterException("No type specified in topology $parameters")
                val topology: Topologies = Topologies.valueOf(type.toUpperCase())
                val currentDependencies = topology.generateDependencies(parameters, this)
                addDependencies(allDependencies, currentDependencies)
            }
        }

        // Add explicit dependencies
        val explicitDependencies = dependencies
        if (explicitDependencies != null) {
            addDependencies(allDependencies, explicitDependencies)
        }

        allDependencies
    }

    private fun addDependencies(to: MutableMap<Int, MutableSet<DependencyConfig>>, from: List<DependencyConfig>) {
        for (dependency in from) {
            val key = dependency.from
            if ((!to.containsKey(key)) || to[key] == null) {
                to[key] = hashSetOf(dependency)
            }
            else {
                to[key]?.add(dependency)
            }
        }
    }
}

