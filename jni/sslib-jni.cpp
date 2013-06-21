/*
 * CrystalExplorer.cpp
 *
 *  Created on: June 10, 2013
 *      Author: Martin Uhrin
 */

#include <jni.h>

#include <sstream>
#include <string>
#include <vector>

#include <armadillo>

// From SSLib
#include <build_cell/AtomsDescription.h>
#include <build_cell/AtomsGenerator.h>
#include <build_cell/StructureBuilder.h>
#include <build_cell/RandomUnitCellGenerator.h>

#include <common/AtomSpeciesDatabase.h>
#include <common/UnitCell.h>

#include <io/ResReaderWriter.h>
#include <io/ResourceLocator.h>

#include <potential/OptimisationSettings.h>
#include <potential/SimplePairPotential.h>
#include <potential/TpsdGeomOptimiser.h>

namespace ssbc = ::sstbx::build_cell;
namespace ssc = ::sstbx::common;
namespace ssio = ::sstbx::io;
namespace ssp = ::sstbx::potential;

extern "C" {

JNIEXPORT jstring JNICALL Java_uk_ac_ucl_phys_crystalexplorer_NdkCrystalExplorer_generateStructure(
  JNIEnv * env,
  jobject javaThis,
  jstring outFile,
  jint numAtoms_,
  jintArray numbers_,
  jfloatArray sizes_,
  jfloatArray strengths_,
  jboolean isCluster
);

}

JNIEXPORT jstring JNICALL Java_uk_ac_ucl_phys_crystalexplorer_NdkCrystalExplorer_generateStructure(
  JNIEnv * env,
  jobject javaThis,
  jstring outFile,
  jint numAtoms_,
  jintArray numbers_,
  jfloatArray sizes_,
  jfloatArray strengths_,
  jboolean isCluster
)
{
	const ::std::string elements[] = {"H", "He", "Li", "Be", "B", "C", "N", "O", "F", "Ne", "Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar", "K", "Ca", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "Ga", "Ge", "As", "Se", "Br", "Kr", "Rb", "Sr", "Y", "Zr", "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb", "Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd", "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb", "Lu", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg", "Tl", "Pb", "Bi", "Po", "At", "Rn", "Fr", "Ra", "Ac", "Th", "Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm", "Md", "No", "Lr", "Rf", "Db", "Sg", "Bh", "Hs", "Mt"};

	int numAtoms = numAtoms_;
	ssio::ResourceLocator structureFile;
	const char * outFileCStr = env->GetStringUTFChars(outFile, NULL);
	structureFile.set(::std::string(outFileCStr));
	env->ReleaseStringUTFChars(outFile, outFileCStr);

	ssc::AtomSpeciesDatabase speciesDb;

	ssbc::StructureBuilder builder;
	if(isCluster == JNI_FALSE)
	{
		::sstbx::UniquePtr<ssbc::RandomUnitCellGenerator>::Type cellGenerator(new ssbc::RandomUnitCellGenerator());
		cellGenerator->setMinAngles(65.0);
		cellGenerator->setMaxAngles(115.0);
		cellGenerator->setContentsMultiplier(2.0);
		builder.setUnitCellGenerator(::sstbx::UniquePtr<ssbc::IUnitCellGenerator>::Type(cellGenerator.release()));
	}

	jint * numbers = env->GetIntArrayElements(numbers_, NULL);
	jfloat * sizes = env->GetFloatArrayElements(sizes_, NULL);
	jfloat * strengths = env->GetFloatArrayElements(strengths_, NULL);

	ssbc::AtomsGeneratorConstructionInfo atomsInfo;
	::std::vector< ::std::string> speciesList;
	::arma::mat epsilon(numAtoms, numAtoms), sigma(numAtoms, numAtoms), beta(numAtoms, numAtoms);
	epsilon.diag().fill(1.0);
	sigma.diag().fill(1.0);
	beta.fill(1.0);

	for(size_t i = 0; i < numAtoms; ++i)
	{
		atomsInfo.atoms.push_back(ssbc::AtomsDescription(elements[i], numbers[i]));
		sigma(i, i) = static_cast<double>(sizes[i]);
		epsilon(i, i) = static_cast<double>(strengths[i]);
		speciesList.push_back(elements[i]);
	}

	env->ReleaseIntArrayElements(numbers_, numbers, 0);
	env->ReleaseFloatArrayElements(sizes_, sizes, 0);
	env->ReleaseFloatArrayElements(strengths_, strengths, 0);

	ssp::TpsdGeomOptimiser optimiser(::sstbx::UniquePtr<ssp::IPotential>::Type(new ssp::SimplePairPotential(speciesDb, speciesList, epsilon, sigma, 2.0, beta, 12.0, 6.0, ssp::CombiningRule::LORENTZ_BERTHELOT)));

	builder.addGenerator(::sstbx::makeUniquePtr(new ssbc::AtomsGenerator(atomsInfo)));

	::sstbx::UniquePtr<ssc::Structure>::Type structure;
	builder.generateStructure(structure, speciesDb);


	ssp::OptimisationSettings optOptions;
	optOptions.maxSteps.reset(2000);
	optimiser.setTolerance(1e-9);
	ssp::OptimisationOutcome outcome = optimiser.optimise(*structure, optOptions);

	if(!outcome.isSuccess())
	{
		::std::stringstream retMsg;
		retMsg << "error: " << outcome.getMessage();
		return env->NewStringUTF(retMsg.str().c_str());
	}

	ssio::ResReaderWriter resWriter;
	resWriter.writeStructure(*structure, structureFile);

	return env->NewStringUTF("success");
}
