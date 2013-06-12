/*
 * SchemaMap.h
 *
 *
 *  Created on: May 29, 2012
 *      Author: Martin Uhrin
 */

#ifndef SCHEMA_MAP_H
#define SCHEMA_MAP_H

// INCLUDES /////////////////////////////////////////////

#include <map>
#include <string>

#include <boost/ptr_container/ptr_map.hpp>

#include "yaml_schema/detail/SchemaElement.h"
#include "utility/HeterogeneousMap.h"

// DEFINES //////////////////////////////////////////////

namespace sstbx {
namespace yaml_schema {

// FORWARD DECLARATIONS ////////////////////////////////////
namespace detail {
template <typename T>
class SchemaHeteroMapEntry;
class SchemaHeteroMapEntryBase;
SchemaHeteroMapEntryBase * new_clone(const SchemaHeteroMapEntryBase & entry);
}

template <typename T>
class SchemaHomoMap : public detail::SchemaElementBase< ::std::map< ::std::string, T> >
{
public:
  typedef ::std::map< ::std::string, T> BindingType;

  virtual bool valueToNode(YAML::Node & node, const BindingType & value, const bool useDefaultOnFail) const;
  virtual bool nodeToValue(SchemaParse & parse, BindingType & value, const YAML::Node & node, const bool useDefaultOnFail) const;

  void addEntry(const ::std::string & name, const detail::SchemaElementBase<T> * const element);

private:
  typedef ::boost::ptr_map< ::std::string, const detail::SchemaElementBase<T> > EntriesMap;

  EntriesMap myEntries;
};

class SchemaHeteroMap : public detail::SchemaElementBase<utility::HeterogeneousMap>
{
public:
  typedef utility::HeterogeneousMap BindingType;

  virtual bool valueToNode(YAML::Node & node, const BindingType & map, const bool useDefaultOnFail) const;
  virtual bool nodeToValue(SchemaParse & parse, BindingType & map, const YAML::Node & node, const bool useDefaultOnFail) const;

  template <typename T>
  detail::SchemaHeteroMapEntry<T> * addEntry(
    const ::std::string & name,
    const utility::Key<T> & key,
    detail::SchemaElementBase<T> * const element
  );
  template <typename T>
  detail::SchemaHeteroMapEntry<T> * addScalarEntry(
    const ::std::string & name,
    const utility::Key<T> & key
  );

  virtual SchemaHeteroMap * clone() const;

private:
  typedef ::boost::ptr_map<const utility::KeyId *, detail::SchemaHeteroMapEntryBase> EntriesMap;

  EntriesMap myEntries;
};


}
}

#include "yaml_schema/detail/SchemaMap.h"

#endif /* SCHEMA_MAP_H */
