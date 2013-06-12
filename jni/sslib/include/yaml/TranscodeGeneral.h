/*
 * TranscodeGeneral.h
 *
 *  Created on: Aug 18, 2011
 *      Author: Martin Uhrin
 */

#ifndef TRANSCODE_GENERAL_H
#define TRANSCODE_GENERAL_H

// INCLUDES //////////////////////////////////
#include "SSLib.h"

#ifdef SSLIB_USE_YAML

#include <boost/regex.hpp>

#include <armadillo>

#include <yaml-cpp/yaml.h>

#include "utility/Range.h"
#include "yaml/HelperTypes.h"

// NAMESPACES ////////////////////////////////

// Some custom YAML transcoders
namespace YAML {

// Vector as string
template <typename T>
struct convert< ::sstbx::yaml::VectorAsString<T> >
{
  static Node encode(const ::sstbx::yaml::VectorAsString<T> & vector);
  static bool decode(const Node & node, ::sstbx::yaml::VectorAsString<T> & vector);
};

// Armadillo fixed vectors
template<unsigned int size>
struct convert<arma::vec::fixed<size> >
{
  static Node encode(const arma::vec::fixed<size> & rhs);
  static bool decode(const Node & node, arma::vec::fixed<size> & rhs);
};

// Armadillo vec
template<>
struct convert<arma::vec>
{
  static Node encode(const arma::vec & rhs);
  static bool decode(const Node& node, arma::vec & rhs);
};

template<>
struct convert< ::sstbx::yaml::ArmaTriangularMat>
{
  static Node encode(const ::sstbx::yaml::ArmaTriangularMat & rhs);
  static bool decode(const Node & node, ::sstbx::yaml::ArmaTriangularMat & rhs);
};

template <typename T>
struct convert< ::sstbx::utility::Range<T> >
{
  static Node encode(const ::sstbx::utility::Range<T> & rhs);
  static bool decode(const Node & node, ::sstbx::utility::Range<T> & rhs);
};

}

#include "yaml/detail/TranscodeGeneral.h"

#endif /* SSLIB_USE_YAML */

#endif /* TRANSCODE_GENERAL_H */
